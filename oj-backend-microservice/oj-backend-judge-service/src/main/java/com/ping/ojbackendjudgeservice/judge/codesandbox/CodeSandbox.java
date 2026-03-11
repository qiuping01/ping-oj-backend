package com.ping.ojbackendjudgeservice.judge.codesandbox;

import com.ping.ojbackendmodel.model.codesandbox.ExecuteCodeRequest;
import com.ping.ojbackendmodel.model.codesandbox.ExecuteCodeResponse;

/**
 * 代码沙箱接口定义
 */
public interface CodeSandbox {

    /**
     * 执行代码
     *
     * @param executeCodeRequest 代码执行请求
     * @return 代码执行响应
     */
    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest);
}
