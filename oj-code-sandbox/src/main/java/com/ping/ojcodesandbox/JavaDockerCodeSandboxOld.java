package com.ping.ojcodesandbox;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DockerClientBuilder;
import com.ping.ojcodesandbox.exception.BusinessException;
import com.ping.ojcodesandbox.model.ExecuteCodeRequest;
import com.ping.ojcodesandbox.model.ExecuteCodeResponse;
import com.ping.ojcodesandbox.model.ExecuteMessage;
import com.ping.ojcodesandbox.model.JudgeInfo;
import com.ping.ojcodesandbox.utils.ProcessUtils;
import org.springframework.util.StopWatch;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Docker 代码沙箱 - 可交互
 */
public class JavaDockerCodeSandboxOld implements CodeSandbox {

    private static final String GLOBAL_CODE_DIR_NAME = "tmpCode";

    private static final String GLOBAL_JAVA_CLASS_NAME = "Main.java";

    private static final long TIME_OUT = 10000L;

    private static final String SECURITY_MANAGER_CLASS_NAME = "MySecurityManager";

    // 拉取镜像开关
    private static final boolean FIRST_INIT = false;

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        List<String> inputList = executeCodeRequest.getInputList();
        String code = executeCodeRequest.getCode();

        // 校验用户代码中是否包含黑名单中的危险命令
        // 1. 把用户的代码保存为文件
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


        // 2. 编译代码，得到 class 文件
        String compileCmd = String.format("javac -encoding utf-8 %s", userCodeFile.getAbsolutePath());
        try {
            Process compileProcess = Runtime.getRuntime().exec(compileCmd);
            ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(compileProcess, "编译");
            System.out.println(executeMessage);
        } catch (Exception e) {
            throw new BusinessException("编译失败", e);
        }

        // ---------------------------------------------------------
        // 3. 创建容器，把文件复制到容器内 - 执行代码，得到输出结果
        DockerClient dockerClient = DockerClientBuilder.getInstance().build();
        // 拉取镜像
        String image = "openjdk:8-alpine";
        if (FIRST_INIT) {
            PullImageCmd pullImageCmd = dockerClient.pullImageCmd(image);
            ResultCallback.Adapter<PullResponseItem> pullAdapter = new ResultCallback.Adapter<PullResponseItem>() {
                @Override
                public void onNext(PullResponseItem item) {
                    System.out.println("下载镜像" + item.getStatus());
                    super.onNext(item);
                }
            };
            // 执行拉取并等待完成
            try {

                pullImageCmd.exec(pullAdapter).awaitCompletion();
                System.out.println("镜像下载完成");

            } catch (InterruptedException e) {
                System.out.println("拉取镜像异常");
                throw new BusinessException("拉取镜像异常", e);
            }
        }
        // 创建容器
        CreateContainerCmd containerCmd = dockerClient.createContainerCmd(image);
        // 挂载用户代码目录到容器内
        HostConfig hostConfig = new HostConfig();
        // 参数1: 宿主机路径，参数2: 容器内路径
        hostConfig.setBinds(new Bind(userCodeParentPath, new Volume("/app")));
        hostConfig.withMemory(100 * 1024 * 1024L); // 限制内存为 100M
        hostConfig.withMemorySwap(0L); // 减少和硬盘的交换
        hostConfig.withReadonlyRootfs(true);  // 在 HostConfig 上设置限制用户向 root 根目录写文件
        hostConfig.withCpuCount(1L);
        CreateContainerResponse createContainerResponse = containerCmd
                .withHostConfig(hostConfig)
                .withNetworkDisabled(true)
                // 把 docker 容器和本地的终端进行一个连接，能够获得本地终端的输入，并向本地终端输出
                .withAttachStdin(true)
                .withAttachStderr(true)
                .withAttachStdout(true)
                .withTty(true) // 创建一个交互终端
//                .withCmd() // 无须添加执行命令，java 执行命令和容器交互再时输入命令
                .exec();
        System.out.println(createContainerResponse);
        String containerId = createContainerResponse.getId();
        // 4. 启动容器,执行代码
        dockerClient.startContainerCmd(containerId).exec();
        List<ExecuteMessage> executeMessageList = new ArrayList<>();
        // 创建执行命令
        // docker exec clever_neumann java -cp /app Main 1 99
        for (String inputArgs : inputList) {
            StopWatch stopWatch = new StopWatch();
            String[] inputArgsArray = inputArgs.split(" ");
            String[] cmdArray = ArrayUtil.append(new String[]{"java", "-cp", "/app", "Main"}, inputArgsArray);
            ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(containerId)
                    .withCmd(cmdArray)
                    .withAttachStdin(true)
                    .withAttachStderr(true)
                    .withAttachStdout(true)
                    .exec();
            System.out.println("创建执行命令：" + execCreateCmdResponse);
            ExecuteMessage executeMessage = new ExecuteMessage();
            final ExecuteMessage finalMsg = executeMessage;  // final 副本，供内部类使用
            String execId = execCreateCmdResponse.getId();
            if (execId == null) {
                return null;
            }
            // 执行命令的回调（处理输出）
            long time = 0;
            ResultCallback.Adapter<Frame> frameAdapter = new ResultCallback.Adapter<Frame>() {
                @Override
                public void onNext(Frame frame) {
                    String payLoad = new String(frame.getPayload(), StandardCharsets.UTF_8);
                    if (StrUtil.isBlank(payLoad.trim())) {
                        return;
                    }
                    StreamType streamType = frame.getStreamType();
                    if (StreamType.STDERR.equals(streamType)) {
                        System.out.println("执行命令错误输出：" + payLoad);
                        finalMsg.setErrorMessage(payLoad);
                    } else {
                        System.out.println("输出结果：" + payLoad.trim());
                        finalMsg.setMessage(payLoad.trim());
                    }
                    super.onNext(frame);
                }
            };
            // --- 统计内存部分 ---
            StatsCmd statsCmd = dockerClient.statsCmd(containerId);
            final long[] maxMemory = {0};
            final CountDownLatch statsLatch = new CountDownLatch(1); // 用于等待至少一次统计
            ResultCallback.Adapter<Statistics> statsAdapter = new ResultCallback.Adapter<Statistics>() {
                @Override
                public void onNext(Statistics statistics) {
                    Long usage = statistics.getMemoryStats().getUsage();
                    if (usage != null) {
                        maxMemory[0] = Math.max(maxMemory[0], usage);
                        finalMsg.setMemory(maxMemory[0]);  // 实时更新为当前最大值
                        System.out.println("占用内存：" + usage);
                    }
                    statsLatch.countDown();  // 第一次收到数据后放行主线程
                    super.onNext(statistics);
                }
            };
            statsCmd.exec(statsAdapter);
            try {
                stopWatch.start();
                // 执行命令
                boolean completed = dockerClient.execStartCmd(execId)
                        .exec(frameAdapter)
                        .awaitCompletion(TIME_OUT, TimeUnit.MILLISECONDS);
                stopWatch.stop();
                time = stopWatch.getLastTaskTimeMillis();
                finalMsg.setTime(time);
                if (!completed) {
                    // 超时处理
                    System.out.println("执行命令超时: " + inputArgs);
                    finalMsg.setErrorMessage("执行超时");
                    // 1. 先关闭 stats 流
                    statsCmd.close();
                    // 2. 停止并删除容器
                    try {
                        dockerClient.stopContainerCmd(containerId).exec();
                        dockerClient.removeContainerCmd(containerId).exec();
                        System.out.println("超时容器已清理");
                    } catch (Exception e) {
                        System.err.println("清理超时容器失败: " + e.getMessage());
                        throw new BusinessException("清理超时容器失败: " + e.getMessage(), e);
                    }
                    // 3. 删除代码文件
                    if (userCodeFile.getParentFile() != null) {
                        FileUtil.del(userCodeFile.getParentFile());
                        System.out.println("超时代码文件已清理");
                    }
                    throw new BusinessException("执行清理代码文件超时");
                }
                // 等待至少一次统计信息（最多1秒），避免因命令执行太快而没收到数据
                statsLatch.await(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                System.out.println("程序执行命令异常");
                throw new BusinessException("程序执行命令异常", e);
            } finally {
                // 关闭统计流，释放资源
                statsCmd.close();
            }
            // --- 统计内存部分结束 ---
            executeMessageList.add(finalMsg);
        }

        // ---------------------------------------------------------
        // 5. 收集整理输出结果
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        List<String> outputList = new ArrayList<>();
        // 取用时最大值，便于判断是否超时
        long MaxTime = 0;
        JudgeInfo judgeInfo = new JudgeInfo();
        for (ExecuteMessage executeMessage : executeMessageList) {
            if (StrUtil.isNotBlank(executeMessage.getErrorMessage())) {
                executeCodeResponse.setMessage(executeMessage.getErrorMessage());
                // 执行中存在错误
                executeCodeResponse.setStatus(3);
                judgeInfo.setMessage("执行中存在错误");
                break;
            }
            // 正常输出直接添加到结果列表
            judgeInfo.setMessage("执行成功");
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
            executeCodeResponse.setStatus(1);
        }
        executeCodeResponse.setOutputList(outputList);
        judgeInfo.setTime(MaxTime);
        executeCodeResponse.setJudgeInfo(judgeInfo);



        // 6. 文件清理，释放空间
        if (userCodeFile.getParentFile() != null) {
            boolean del = FileUtil.del(userCodeFile.getParentFile());
            System.out.println("删除文件结果：" + (del ? "成功" : "失败"));
        }
        // 2. 停止并删除容器
        try {
            dockerClient.stopContainerCmd(containerId).exec();
            dockerClient.removeContainerCmd(containerId).exec();
            System.out.println("执行完成，容器已清理");
        } catch (Exception e) {
            System.err.println("执行完成，清理容器失败: " + e.getMessage());
            throw new BusinessException("执行完成，清理容器失败: " + e.getMessage(), e);
        }
        return executeCodeResponse;
    }

    public static void main(String[] args) {
        JavaDockerCodeSandboxOld javaNativeCodeSandbox = new JavaDockerCodeSandboxOld();
        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
        executeCodeRequest.setInputList(Arrays.asList("1 2", "1 3"));
        executeCodeRequest.setLanguage("java");
//        String code = ResourceUtil.readStr("testCode/simpleComputeArgs/Main.java", StandardCharsets.UTF_8);
//        String code = ResourceUtil.readStr("testCode/timeOut/Main.java", StandardCharsets.UTF_8);
        String code = ResourceUtil.readStr("testCode/netCode/Main.java",
                StandardCharsets.UTF_8);
//        String code = ResourceUtil.readStr("testCodeScanner/Main.java", StandardCharsets.UTF_8);
        executeCodeRequest.setCode(code);
        ExecuteCodeResponse executeCodeResponse = javaNativeCodeSandbox.executeCode(executeCodeRequest);
        System.out.println(executeCodeResponse);
    }
}
