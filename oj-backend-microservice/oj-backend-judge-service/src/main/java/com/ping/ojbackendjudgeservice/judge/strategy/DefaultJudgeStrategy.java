package com.ping.ojbackendjudgeservice.judge.strategy;

import cn.hutool.json.JSONUtil;
import com.ping.ojbackendmodel.model.codesandbox.JudgeInfo;
import com.ping.ojbackendmodel.model.dto.question.JudgeCase;
import com.ping.ojbackendmodel.model.dto.question.JudgeConfig;
import com.ping.ojbackendmodel.model.entity.Question;
import com.ping.ojbackendmodel.model.enums.JudgeInfoMessageEnum;

import java.util.List;

/**
 * 默认判题策略
 */
public class DefaultJudgeStrategy implements JudgeStrategy {
    @Override
    public JudgeInfo doJudge(JudgeContext judgeContext) {
        // 获取判题参数
        JudgeInfo judgeInfo = judgeContext.getJudgeInfo();
        Long memory = judgeInfo.getMemory();
        Long time = judgeInfo.getTime();
        List<String> inputList = judgeContext.getInputList();
        List<String> outputList = judgeContext.getOutputList();
        Question question = judgeContext.getQuestion();
        List<JudgeCase> judgeCaseList = judgeContext.getJudgeCaseList();
        // 设置判题信息
        JudgeInfo judgeInfoResponse = new JudgeInfo();
        JudgeInfoMessageEnum judgeInfoMessageEnum = JudgeInfoMessageEnum.WAITING;
        judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
        judgeInfoResponse.setMemory(memory);
        judgeInfoResponse.setTime(time);
        // 先判断沙箱执行的结果输出数量是否和预期数量相等
        if (outputList.size() != inputList.size()) {
            judgeInfoMessageEnum = JudgeInfoMessageEnum.WRONG_ANSWER;
            judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
            return judgeInfoResponse;
        }
        // 依次判断每一项输出和预期输出是否相等
        for (int i = 0; i < judgeCaseList.size(); i++) {
            JudgeCase judgeCase = judgeCaseList.get(i);
            if (!judgeCase.getOutput().equals(outputList.get(i))) {
                judgeInfoMessageEnum = JudgeInfoMessageEnum.WRONG_ANSWER;
                judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
                return judgeInfoResponse;
            }
        }
        // 判断题目限制
        String judgeConfigStr = question.getJudgeConfig();
        JudgeConfig judgeConfig = JSONUtil.toBean(judgeConfigStr, JudgeConfig.class);
        Long needTimeLimit = judgeConfig.getTimeLimit();
        Long needMemoryLimit = judgeConfig.getMemoryLimit();
        if (memory > needMemoryLimit) {
            judgeInfoMessageEnum = JudgeInfoMessageEnum.MEMORY_LIMIT_EXCEEDED;
            judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
            return judgeInfoResponse;
        }
        if (time > needTimeLimit) {
            judgeInfoMessageEnum = JudgeInfoMessageEnum.TIME_LIMIT_EXCEEDED;
            judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
            return judgeInfoResponse;
        }
        // 最终判题通过设置 AC
        judgeInfoMessageEnum = JudgeInfoMessageEnum.ACCEPTED;
        judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
        return judgeInfoResponse;
    }
}
