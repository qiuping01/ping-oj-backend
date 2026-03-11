package com.ping.ojcodesandbox.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final int statusCode;
    private final String errorMessage;

    /**
     * 构造业务异常
     *
     * @param errorMessage 错误信息
     */
    public BusinessException(String errorMessage) {
        super(errorMessage);
        this.statusCode = 3;
        this.errorMessage = errorMessage;
    }

    /**
     * 构造业务异常（带状态码）
     *
     * @param statusCode   状态码
     * @param errorMessage 错误信息
     */
    public BusinessException(int statusCode, String errorMessage) {
        super(errorMessage);
        this.statusCode = statusCode;
        this.errorMessage = errorMessage;
    }

    /**
     * 构造业务异常（带原始异常）
     *
     * @param errorMessage 错误信息
     * @param cause        原始异常
     */
    public BusinessException(String errorMessage, Throwable cause) {
        super(errorMessage, cause);
        this.statusCode = 3;
        this.errorMessage = errorMessage;
    }
}