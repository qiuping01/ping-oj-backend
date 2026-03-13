package com.ping.oj.judge.strategy;

import cn.hutool.json.JSONUtil;
import com.ping.oj.model.dto.question.JudgeCase;
import com.ping.oj.model.dto.question.JudgeConfig;
import com.ping.oj.model.dto.questionsubmit.JudgeInfo;
import com.ping.oj.model.entity.Question;
import com.ping.oj.model.enums.JudgeInfoMessageEnum;

import java.util.List;
import java.util.Optional;

/**
 * 默认判题策略
 */
public class JavaLanguageJudgeStrategy implements JudgeStrategy {
    @Override
    public JudgeInfo doJudge(JudgeContext judgeContext) {
        // 获取判题参数
        JudgeInfo judgeInfo = judgeContext.getJudgeInfo();
        Long memory = null;
        Long time = null;
        if (judgeInfo == null) {
            judgeInfo = new JudgeInfo();
            judgeInfo.setMemory(null);
            judgeInfo.setTime(null);
        } else {
            memory = Optional.ofNullable(judgeInfo.getMemory()).orElse(0L);
            time = Optional.ofNullable(judgeInfo.getTime()).orElse(0L);
        }
        List<String> inputList = judgeContext.getInputList();
        List<String> outputList = judgeContext.getOutputList();
        Question question = judgeContext.getQuestion();
        List<JudgeCase> judgeCaseList = judgeContext.getJudgeCaseList();
        String judgeInfoMessage = judgeContext.getJudgeInfoMessage();
        // 设置判题信息
        JudgeInfo judgeInfoResponse = new JudgeInfo();
        // 如果 judgeInfoMessage 不为空，则赋值给 judgeInfoResponse
        if (judgeInfoMessage != null) {
            judgeInfoResponse.setMessage(judgeInfoMessage);
            return judgeInfoResponse;
        }
        JudgeInfoMessageEnum judgeInfoMessageEnum = JudgeInfoMessageEnum.WAITING;
        judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
        judgeInfoResponse.setMemory(memory);
        judgeInfoResponse.setTime(time);
        // 增加超时判断
        // Java 程序本身需要额外执行 10 秒钟
        long JAVA_PROGRAM_TIME_COST = 10000L;
        if (time >= JAVA_PROGRAM_TIME_COST) {
            judgeInfoMessageEnum = JudgeInfoMessageEnum.TIME_LIMIT_EXCEEDED;
            judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
            return judgeInfoResponse;
        }
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
        if ((time - JAVA_PROGRAM_TIME_COST) > needTimeLimit) {
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
