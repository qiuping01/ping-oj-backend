package com.ping.ojbackendjudgeservice.judge;

import com.ping.ojbackendjudgeservice.judge.strategy.DefaultJudgeStrategy;
import com.ping.ojbackendjudgeservice.judge.strategy.JavaLanguageJudgeStrategy;
import com.ping.ojbackendjudgeservice.judge.strategy.JudgeContext;
import com.ping.ojbackendjudgeservice.judge.strategy.JudgeStrategy;
import com.ping.ojbackendmodel.model.codesandbox.JudgeInfo;
import com.ping.ojbackendmodel.model.entity.QuestionSubmit;
import com.ping.ojbackendmodel.model.enums.QuestionSubmitLanguageEnum;
import org.springframework.stereotype.Service;

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
