package com.ping.ojcodesandbox.controller;

import com.ping.ojcodesandbox.JavaNativeCodeSandbox;
import com.ping.ojcodesandbox.exception.BusinessException;
import com.ping.ojcodesandbox.model.ExecuteCodeRequest;
import com.ping.ojcodesandbox.model.ExecuteCodeResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 主接口
 */
@RestController("/")
public class MainController {

    @Resource
    private JavaNativeCodeSandbox javaNativeCodeSandbox;

    @GetMapping("/health")
    public String healthCheck() {
        return "Hello ";
    }

    /**
     * 执行代码
     *
     * @param executeCodeRequest
     * @return
     */
    @PostMapping("executeCode")
    public ExecuteCodeResponse executeCode(@RequestBody ExecuteCodeRequest executeCodeRequest){
        if (executeCodeRequest == null) {
            throw new BusinessException("参数不能为空");
        }
        return javaNativeCodeSandbox.executeCode(executeCodeRequest);
    }
}
