package com.ping.ojbackendjudgeservice.judge.codesandbox.impl;

import com.ping.ojbackendjudgeservice.judge.codesandbox.CodeSandbox;
import com.ping.ojbackendmodel.model.codesandbox.ExecuteCodeRequest;
import com.ping.ojbackendmodel.model.codesandbox.ExecuteCodeResponse;
import com.ping.ojbackendmodel.model.codesandbox.JudgeInfo;
import com.ping.ojbackendmodel.model.enums.JudgeInfoMessageEnum;
import com.ping.ojbackendmodel.model.enums.QuestionSubmitStatusEnum;

import java.util.List;

/**
 * 示例代码沙箱（仅为了跑通业务流程）
 */
public class ExampleCodeSandbox implements CodeSandbox {
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        List<String> inputList = executeCodeRequest.getInputList();
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setMessage(JudgeInfoMessageEnum.ACCEPTED.getText());
        judgeInfo.setMemory(100L);
        judgeInfo.setTime(100L);
        ExecuteCodeResponse response = new ExecuteCodeResponse();
        response.setOutputList(inputList);
        response.setMessage("示例沙箱 - 测试执行成功");
        response.setStatus(QuestionSubmitStatusEnum.SUCCEED.getValue());
        response.setJudgeInfo(judgeInfo);
        return response;
    }
}
