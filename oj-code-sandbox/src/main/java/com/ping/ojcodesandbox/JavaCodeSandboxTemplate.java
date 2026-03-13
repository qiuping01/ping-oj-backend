package com.ping.ojcodesandbox;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.dfa.FoundWord;
import cn.hutool.dfa.WordTree;
import com.ping.ojcodesandbox.exception.BusinessException;
import com.ping.ojcodesandbox.model.*;
import com.ping.ojcodesandbox.utils.ProcessUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public abstract class JavaCodeSandboxTemplate implements CodeSandbox {

    private static final String GLOBAL_CODE_DIR_NAME = "tmpCode";

    private static final String GLOBAL_JAVA_CLASS_NAME = "Main.java";

    private static final long TIME_OUT = 10000L;

    // 黑名单过滤危险命令
    private static final List<String> blackList = Arrays.asList("Files", "exec");

    private static final WordTree WORD_TREE;

    static {
        // 初始化字典树
        WORD_TREE = new WordTree();
        WORD_TREE.addWords(blackList);
    }

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        List<String> inputList = executeCodeRequest.getInputList();
        String code = executeCodeRequest.getCode();

        // 1. 校验用户代码中是否包含黑名单中的危险命令
        checkUnsafeCommand(code);

        // 2. 把用户的代码保存为文件
        File userCodeFile = saveCodeToFile(code);

        // 3. 编译代码，得到 class 文件
        compileFile(userCodeFile);

        // 4. 执行代码，得到输出结果 - 子类可修改实现
        List<ExecuteMessage> executeMessageList = runFile(inputList, userCodeFile);

        // 5. 收集整理输出结果
        ExecuteCodeResponse executeCodeResponse = getOutputResponse(executeMessageList);

        // 6. 文件清理，释放空间 - 子类可修改实现
        cleanFile(userCodeFile);
        return executeCodeResponse;
    }

    // 1. 校验用户代码中是否包含黑名单中的危险命令
    protected void checkUnsafeCommand(String code) {
        FoundWord foundWord = WORD_TREE.matchWord(code);
        if (foundWord != null) {
            System.out.println("用户代码中包含危险命令：" + foundWord.getFoundWord());
            String dangerousOperationValue = JudgeInfoMessageEnum.DANGEROUS_OPERATION.getValue();
            throw new BusinessException("用户代码中包含危险命令：" + foundWord.getFoundWord(), dangerousOperationValue);
        }
    }

    // 2. 把用户的代码保存为文件
    protected File saveCodeToFile(String code) {
        String userDir = System.getProperty("user.dir");
        // 使用 File.separator 兼容不同系统的目录斜杠线
        String globalCodePathName = userDir + File.separator + GLOBAL_CODE_DIR_NAME;
        // 判断全局代码目录是否存在，没有则新建
        if (!FileUtil.exist(globalCodePathName)) {
            FileUtil.mkdir(globalCodePathName);
        }
        // 每个用户的提交代码需要隔离存放在不同的文件夹中
        String userCodeParentPath = globalCodePathName + File.separator + UUID.randomUUID();
        // 存入代码文件
        String userCodePath = userCodeParentPath + File.separator + GLOBAL_JAVA_CLASS_NAME;
        File userCodeFile = FileUtil.writeString(code, userCodePath, StandardCharsets.UTF_8);
        return userCodeFile;
    }

    // 3. 编译代码，得到 class 文件
    protected void compileFile(File userCodeFile) {
        String compileCmd = String.format("javac -encoding utf-8 %s", userCodeFile.getAbsolutePath());
        String compileErrorValue = JudgeInfoMessageEnum.COMPILE_ERROR.getValue();
        try {
            Process compileProcess = Runtime.getRuntime().exec(compileCmd);
            ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(compileProcess, "编译");
            System.out.println(executeMessage);
            // 检查编译是否成功
            if (StrUtil.isNotBlank(executeMessage.getErrorMessage())) {
                throw new BusinessException("编译错误：" + executeMessage.getErrorMessage(), compileErrorValue);
            }
        } catch (Exception e) {
            throw new BusinessException("编译失败", e, compileErrorValue);
        }
    }


    /**
     * 4. 执行代码，得到输出结果
     *
     * @param inputList 输入用例
     * @return 执行结果列表
     */
    protected List<ExecuteMessage> runFile(List<String> inputList, File userCodeFile) {
        String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();
        List<ExecuteMessage> executeMessageList = new ArrayList<>();
        String timeLimitExceededValue = JudgeInfoMessageEnum.TIME_LIMIT_EXCEEDED.getValue();
        String runtimeErrorValue = JudgeInfoMessageEnum.RUNTIME_ERROR.getValue();
        String presentationErrorValue = JudgeInfoMessageEnum.PRESENTATION_ERROR.getValue();
        for (String inputArgs : inputList) {
            String runCmd = String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s Main %s", userCodeParentPath, inputArgs);
//            String runCmd = String.format("java -Dfile.encoding=UTF-8 -cp %s;%s -Djava.security.manager=%s Main %s", userCodeParentPath, SECURITY_MANAGER_PATH, SECURITY_MANAGER_CLASS_NAME, inputArgs);
            try {
                Process runProcess = Runtime.getRuntime().exec(runCmd);
                // 超时控制
                new Thread(() -> {
                    try {
                        Thread.sleep(TIME_OUT);
                        System.out.println("超时，终止进程");
                        runProcess.destroy();
                        throw new BusinessException("超时终止进程", runtimeErrorValue);
                    } catch (InterruptedException e) {
                        throw new BusinessException("超时终止进程失败", e, timeLimitExceededValue);
                    }
                }).start();
                ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(runProcess, "运行");
                executeMessageList.add(executeMessage);
//                ExecuteMessage executeMessage = ProcessUtils.runInteractProcessAndGetMessage(runProcess, "运行", inputArgs);
                System.out.println(executeMessage);
                if (StrUtil.isNotBlank(executeMessage.getErrorMessage())) {
                    throw new BusinessException("格式错误：" + executeMessage.getErrorMessage(), presentationErrorValue);
                }
            } catch (Exception e) {
                throw new BusinessException("执行运行失败", e, runtimeErrorValue);
            }
        }
        return executeMessageList;
    }

    // 5. 收集整理输出结果
    protected ExecuteCodeResponse getOutputResponse(List<ExecuteMessage> executeMessageList) {
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        List<String> outputList = new ArrayList<>();
        // 取用时最大值，便于判断是否超时
        long MaxTime = 0;
        JudgeInfo judgeInfo = new JudgeInfo();
        String systemErrorValue = JudgeInfoMessageEnum.SYSTEM_ERROR.getValue();
        for (ExecuteMessage executeMessage : executeMessageList) {
            if (StrUtil.isNotBlank(executeMessage.getErrorMessage())) {
                executeCodeResponse.setMessage(executeMessage.getErrorMessage());
                // 执行中存在错误
                executeCodeResponse.setStatus(3);
                judgeInfo.setMessage(systemErrorValue);
                break;
            }
            // 正常输出直接添加到结果列表
            outputList.add(executeMessage.getMessage());
            Long time = executeMessage.getTime();
            // 记录最大执行时间
            if (time != null) {
                MaxTime = Math.max(MaxTime, time);
            }
            judgeInfo.setMemory(executeMessage.getMemory());
        }
        // 正常运行完成
        if (outputList.size() == executeMessageList.size()) {
            executeCodeResponse.setStatus(2);
        }
        executeCodeResponse.setOutputList(outputList);
//        judgeInfo.setMemory(); // 需要调用第三方库实现，较复杂，暂不实现
        judgeInfo.setTime(MaxTime);
        executeCodeResponse.setJudgeInfo(judgeInfo);
        return executeCodeResponse;
    }

    // 6. 文件清理，释放空间
    protected void cleanFile(File userCodeFile) {
        if (userCodeFile.getParentFile() != null) {
            boolean del = FileUtil.del(userCodeFile.getParentFile());
            System.out.println("删除文件结果：" + (del ? "成功" : "失败"));
        }
    }
}
