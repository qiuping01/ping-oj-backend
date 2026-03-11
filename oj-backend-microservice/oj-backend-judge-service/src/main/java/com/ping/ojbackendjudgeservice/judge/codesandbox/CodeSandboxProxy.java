package com.ping.ojbackendjudgeservice.judge.codesandbox;

import com.ping.ojbackendmodel.model.codesandbox.ExecuteCodeRequest;
import com.ping.ojbackendmodel.model.codesandbox.ExecuteCodeResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 代码沙箱代理（增强代码沙箱请求与响应日志）
 */
@Slf4j
public class CodeSandboxProxy implements CodeSandbox {

    private final CodeSandbox codeSandbox;

    public CodeSandboxProxy(CodeSandbox codeSandbox) {
        this.codeSandbox = codeSandbox;
    }

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        log.info("代码沙箱请求信息：{}", executeCodeRequest.toString());
        ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
        log.info("代码沙箱响应信息：{}", executeCodeResponse.toString());
        return executeCodeResponse;
    }
}
