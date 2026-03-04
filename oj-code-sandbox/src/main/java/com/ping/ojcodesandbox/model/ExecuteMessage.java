package com.ping.ojcodesandbox.model;

import lombok.Data;

/**
 * 进程执行信息
 */
@Data
public class ExecuteMessage {

    /**
     * 进程执行结果
     */
    private Integer exitValue;

    /**
     * 进程执行输出
     */
    private String message;

    /**
     * 进程执行错误
     */
    private String errorMessage;
}
