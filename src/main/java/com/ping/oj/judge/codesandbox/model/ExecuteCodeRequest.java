package com.ping.oj.judge.codesandbox.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 执行代码请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteCodeRequest {

    /**
     * 一组输入用例
     */
    private List<String> inputList;

    /**
     * 编程的语言
     */
    private String language;

    /**
     * 执行的代码
     */
    private String code;
}
