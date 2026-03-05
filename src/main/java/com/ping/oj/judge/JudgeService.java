package com.ping.oj.judge;

import com.ping.oj.model.entity.QuestionSubmit;

/**
 * 判题服务
 */
public interface JudgeService {

    /**
     * 根据题目提交 id 进行判题
     *
     * @param questionSubmitId 题目提交 ID
     * @return 题目提交 - 内含判题信息
     */
    QuestionSubmit doJudge(long questionSubmitId);
}
