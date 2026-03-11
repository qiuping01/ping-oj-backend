package com.ping.ojbackendjudgeservice.judge.strategy;

import com.ping.ojbackendmodel.model.codesandbox.JudgeInfo;
import com.ping.ojbackendmodel.model.dto.question.JudgeCase;
import com.ping.ojbackendmodel.model.entity.Question;
import com.ping.ojbackendmodel.model.entity.QuestionSubmit;
import lombok.Data;

import java.util.List;

/**
 * 用于定义在策略中要传递的参数
 */
@Data
public class JudgeContext {

    /**
     * 判题信息
     */
    private JudgeInfo judgeInfo;

    /**
     * 输入用例
     */
    private List<String> inputList;

    /**
     * 输出用例
     */
    private List<String> outputList;

    /**
     * 判题用例
     */
    private List<JudgeCase> judgeCaseList;

    /**
     * 题目
     */
    private Question question;

    /**
     * 题目提交
     */
    private QuestionSubmit questionSubmit;
}
