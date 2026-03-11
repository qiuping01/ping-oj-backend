package com.ping.ojcodesandbox.exception;

import com.ping.ojcodesandbox.model.ExecuteCodeResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ExecuteCodeResponse handleBusinessException(BusinessException e) {
        ExecuteCodeResponse response = new ExecuteCodeResponse();
        response.setStatus(e.getStatusCode());
        response.setMessage(e.getErrorMessage());
        log.error("业务异常: {}", e.getErrorMessage(), e);
        return response;
    }

    /**
     * 处理其他未预期异常
     */
    @ExceptionHandler(Exception.class)
    public ExecuteCodeResponse handleException(Exception e) {
        ExecuteCodeResponse response = new ExecuteCodeResponse();
        response.setStatus(3);
        response.setMessage("系统内部错误: " + e.getMessage());
        log.error("系统异常", e);
        return response;
    }
}