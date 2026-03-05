package com.ping.oj.judge;

import com.ping.oj.judge.strategy.DefaultJudgeStrategy;
import com.ping.oj.judge.strategy.JavaLanguageJudgeStrategy;
import com.ping.oj.judge.strategy.JudgeContext;
import com.ping.oj.judge.strategy.JudgeStrategy;
import com.ping.oj.model.dto.question.JudgeCase;
import com.ping.oj.model.dto.questionsubmit.JudgeInfo;
import com.ping.oj.model.entity.Question;
import com.ping.oj.model.entity.QuestionSubmit;
import com.ping.oj.model.enums.QuestionSubmitLanguageEnum;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 判题管理 (简化判题策略调用)
 */
@Service
public class JudgeManager {

    JudgeInfo doJudge(JudgeContext judgeContext) {
        // 获取参数
        QuestionSubmit questionSubmit = judgeContext.getQuestionSubmit();
        String language = questionSubmit.getLanguage();
        // 执行判题策略
        JudgeStrategy judgeStrategy = new DefaultJudgeStrategy();
        if (language.equals(QuestionSubmitLanguageEnum.JAVA.getValue())){
            judgeStrategy = new JavaLanguageJudgeStrategy();
        }
        return judgeStrategy.doJudge(judgeContext);
    }
}
