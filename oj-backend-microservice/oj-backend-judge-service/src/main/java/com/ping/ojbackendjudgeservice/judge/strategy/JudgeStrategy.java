package com.ping.ojbackendjudgeservice.judge.strategy;

import com.ping.ojbackendmodel.model.codesandbox.JudgeInfo;

/**
 * 判题策略接口
 */
public interface JudgeStrategy {

    /**
     * 执行判题
     *
     * @param judgeContext 判题上下文
     * @return 判题信息
     */
    JudgeInfo doJudge(JudgeContext judgeContext);

}
