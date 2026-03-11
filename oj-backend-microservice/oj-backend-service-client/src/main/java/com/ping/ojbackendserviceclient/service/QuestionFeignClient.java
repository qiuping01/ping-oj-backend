package com.ping.ojbackendserviceclient.service;

import com.ping.ojbackendmodel.model.entity.Question;
import com.ping.ojbackendmodel.model.entity.QuestionSubmit;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 题目服务
 */
@FeignClient(name = "oj-backend-question-service", path = "/api/question/inner")
public interface QuestionFeignClient {

    // questionService.getById(questionId)
    // questionSubmitService.getById(questionSubmitId)
    // questionSubmitService.updateById(questionSubmitUpdate)

    /**
     * 根据 ID 获取题目
     *
     * @param questionId
     * @return
     */
    @GetMapping("/get/id")
    Question getQuestionById(@RequestParam("questionId") long questionId);

    /**
     * 根据 ID 获取题目提交
     *
     * @param questionSubmitId
     * @return
     */
    @GetMapping("/question_submit/get/id")
    QuestionSubmit getQuestionSubmitById(@RequestParam("questionSubmitId") long questionSubmitId);

    /**
     * 更新题目提交
     *
     * @param questionSubmit
     * @return
     */
    @PostMapping("/question_submit/update")
    boolean updateQuestionSubmitById(@RequestBody QuestionSubmit questionSubmit);
}
