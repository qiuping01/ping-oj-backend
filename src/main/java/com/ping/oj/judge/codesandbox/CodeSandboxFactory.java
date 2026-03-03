package com.ping.oj.judge.codesandbox;

import com.ping.oj.judge.codesandbox.impl.ExampleCodeSandbox;
import com.ping.oj.judge.codesandbox.impl.RemoteCodeSandbox;
import com.ping.oj.judge.codesandbox.impl.ThirdPartyCodeSandbox;

/**
 * 代码沙箱工厂（根据字符串参数创建指定的代码沙箱实例）
 */
public class CodeSandboxFactory {

    /**
     * 创建代码沙箱实例
     *
     * @param type 代码沙箱类型
     * @return 代码沙箱实例
     */
    public static CodeSandbox newInstance(String type) {
        switch (type) {
            case "example":
                return new ExampleCodeSandbox();
            case "remote":
                return new RemoteCodeSandbox();
            case "thirdParty":
                return new ThirdPartyCodeSandbox();
            default:
                return new ExampleCodeSandbox();
        }
    }
}
