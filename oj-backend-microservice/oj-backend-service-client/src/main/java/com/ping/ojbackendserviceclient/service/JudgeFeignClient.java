package com.ping.ojbackendserviceclient.service;

import com.ping.ojbackendmodel.model.entity.QuestionSubmit;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 判题服务
 */
@FeignClient(name = "oj-backend-judge-service", path = "/api/judge/inner")
public interface JudgeFeignClient {

    /**
     * 根据题目提交 id 进行判题
     *
     * @param questionSubmitId 题目提交 ID
     * @return 题目提交 - 内含判题信息
     */
    @PostMapping("/do")
    QuestionSubmit doJudge(@RequestParam("questionSubmitId") long questionSubmitId);
}
