package com.ping.ojcodesandbox;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DockerClientBuilder;
import com.ping.ojcodesandbox.exception.BusinessException;
import com.ping.ojcodesandbox.model.ExecuteMessage;
import com.ping.ojcodesandbox.model.JudgeInfoMessageEnum;
import org.springframework.util.StopWatch;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Docker 代码沙箱 - 可交互
 */
public class JavaDockerCodeSandbox extends JavaCodeSandboxTemplate {

    private static final long TIME_OUT = 10000L;

    // 拉取镜像开关
    private static final boolean FIRST_INIT = false;

    private static final DockerClient dockerClient = DockerClientBuilder.getInstance().build();

    // 改为实例变量
    private String containerId = "";

    @Override
    protected void cleanFile(File userCodeFile) {
        // 清理容器
        cleanupContainer();
        // 最后清理文件（调用父类）
        super.cleanFile(userCodeFile);
    }

    /**
     * 清理容器（独立方法，便于复用）
     */
    private void cleanupContainer() {
        if (containerId == null || containerId.isEmpty()) {
            return;
        }
        try {
            dockerClient.stopContainerCmd(containerId).exec();
            dockerClient.removeContainerCmd(containerId).exec();
            System.out.println("容器已清理: " + containerId);
        } catch (Exception e) {
            System.err.println("清理容器失败: " + e.getMessage());
            // 不抛出异常，避免影响主流程
        } finally {
            containerId = "";  // 重置
        }
    }

    @Override
    protected List<ExecuteMessage> runFile(List<String> inputList, File userCodeFile) {
        String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();
        // 3. 创建容器，把文件复制到容器内 - 执行代码，得到输出结果
        // 拉取镜像
        String image = "openjdk:8-alpine";
        String systemErrorValue = JudgeInfoMessageEnum.SYSTEM_ERROR.getValue();
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
                throw new BusinessException("拉取镜像异常", e, systemErrorValue);
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
        containerId = createContainerResponse.getId();
        // 4. 启动容器,执行代码
        dockerClient.startContainerCmd(containerId).exec();
        List<ExecuteMessage> executeMessageList = new ArrayList<>();
        String runtimeErrorValue = JudgeInfoMessageEnum.RUNTIME_ERROR.getValue();
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
                throw new BusinessException("创建执行命令失败：execId 为空", runtimeErrorValue);
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
                    System.out.println("执行命令超时: " + inputArgs);
                    finalMsg.setErrorMessage("执行超时");
                    // 快速等待一下内存统计
                    try {
                        statsLatch.await(500, TimeUnit.MILLISECONDS);  // 最多等500ms
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                    // 清理容器
                    cleanupContainer();
                    executeMessageList.add(finalMsg);
                    return executeMessageList;
                }
                // 等待至少一次统计信息（最多1秒），避免因命令执行太快而没收到数据
                statsLatch.await(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                System.out.println("程序执行命令异常");
                throw new BusinessException("程序执行命令异常", e, runtimeErrorValue);
            } finally {
                // 关闭统计流，释放资源
                statsCmd.close();
            }
            // --- 统计内存部分结束 ---
            executeMessageList.add(finalMsg);
        }
        return executeMessageList;
    }
}
