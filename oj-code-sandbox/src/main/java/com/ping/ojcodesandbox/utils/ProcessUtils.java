package com.ping.ojcodesandbox.utils;

import com.ping.ojcodesandbox.model.ExecuteMessage;

import java.io.*;

/**
 * 进程工具类 - 用于获取程序执行时的输出信息
 */
public class ProcessUtils {

    /**
     * 执行进程并获取信息
     *
     * @param runProcess 正在执行的进程
     * @param opName     操作名称
     * @return 执行信息
     */
    public static ExecuteMessage runProcessAndGetMessage(Process runProcess, String opName) {
        ExecuteMessage executeMessage = new ExecuteMessage();
        try {
            // 等待程序执行，获取错误码
            int exitValue = runProcess.waitFor();
            executeMessage.setExitValue(exitValue);
            // 正常退出
            if (exitValue == 0) {
                System.out.println(opName + "成功");
                // 创建输入流的读取器，包装输入流，成块分批的去输出
                // 分批获取进程的正常输出
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));
                StringBuilder compileOutputStringBuilder = new StringBuilder();
                // 逐行读取
                String compileOutputLine;
                while ((compileOutputLine = bufferedReader.readLine()) != null) {
                    System.out.println(compileOutputLine);
                    compileOutputStringBuilder.append(compileOutputLine);
                }
                System.out.println(compileOutputStringBuilder);
                executeMessage.setMessage(compileOutputStringBuilder.toString());
            } else {
                // 异常退出
                System.out.println(opName + "失败,错误码：" + exitValue);
                // 分批获取进程的正常输出
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));
                StringBuilder compileOutputStringBuilder = new StringBuilder();
                // 逐行读取
                String compileOutputLine;
                while ((compileOutputLine = bufferedReader.readLine()) != null) {
                    System.out.println(compileOutputLine);
                    compileOutputStringBuilder.append(compileOutputLine);
                }
                // 分批获取进程的错误输出
                BufferedReader errorBufferedReader = new BufferedReader(new InputStreamReader(runProcess.getErrorStream()));
                StringBuilder errorCompileOutputStringBuilder = new StringBuilder();
                // 逐行读取
                String errorOutputLine;
                while ((errorOutputLine = errorBufferedReader.readLine()) != null) {
                    System.out.println(errorOutputLine);
                    errorCompileOutputStringBuilder.append(errorOutputLine);
                }
                System.out.println(errorCompileOutputStringBuilder);
                executeMessage.setErrorMessage(errorCompileOutputStringBuilder.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return executeMessage;
    }

    /**
     * 执行进程并获取信息 - 用于交互式进程
     *
     * @param runProcess 正在执行的进程
     * @param opName     操作名称
     * @param args       交互式参数
     * @return 执行信息
     */
    public static ExecuteMessage runInteractProcessAndGetMessage(Process runProcess, String opName,
                                                                 String args) {
        ExecuteMessage executeMessage = new ExecuteMessage();
        try {
            // 向控制台输入程序
            OutputStream outputStream = runProcess.getOutputStream();
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
            outputStreamWriter.write(args);
            // 注意打上换行符
            outputStreamWriter.write("\n");
            // 相当于按了回车，执行输入的信息
            outputStreamWriter.flush();
            // 等待程序执行一小段时间
            Thread.sleep(100);
            // 分批获取进程的正常输出
            InputStream inputStream = runProcess.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder compileOutputStringBuilder = new StringBuilder();
            // 逐行读取
            String compileOutputLine;
            while ((compileOutputLine = bufferedReader.readLine()) != null) {
                System.out.println(compileOutputLine);
                compileOutputStringBuilder.append(compileOutputLine);
            }
            System.out.println(compileOutputStringBuilder);
            executeMessage.setMessage(compileOutputStringBuilder.toString());
            // 资源回收
            outputStreamWriter.close();
            outputStream.close();
            inputStream.close();
            runProcess.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return executeMessage;
    }
}

