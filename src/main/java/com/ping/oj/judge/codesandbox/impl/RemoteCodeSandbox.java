package com.ping.oj.judge.codesandbox.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.ping.oj.common.ErrorCode;
import com.ping.oj.exception.BusinessException;
import com.ping.oj.judge.codesandbox.CodeSandbox;
import com.ping.oj.judge.codesandbox.model.ExecuteCodeRequest;
import com.ping.oj.judge.codesandbox.model.ExecuteCodeResponse;

/**
 * 远程代码沙箱（实际调用接口的沙箱）
 */
public class RemoteCodeSandbox implements CodeSandbox {
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        System.out.println("远程代码沙箱");
        String url = "http://localhost:8090/executeCode";
        String jsonStr = JSONUtil.toJsonStr(executeCodeRequest);
        String responseStr = HttpUtil.createPost(url)
                .body(jsonStr)
                .execute()
                .body();
        if (StrUtil.isBlank(responseStr)){
            throw new BusinessException(ErrorCode.API_REQUEST_ERROR, "远程代码沙箱请求失败");
        }
        return JSONUtil.toBean(responseStr, ExecuteCodeResponse.class);
    }
}
