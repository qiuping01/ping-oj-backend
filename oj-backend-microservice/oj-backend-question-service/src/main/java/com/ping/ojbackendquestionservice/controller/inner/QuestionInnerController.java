package com.ping.ojbackendquestionservice.controller.inner;

import com.ping.ojbackendmodel.model.entity.Question;
import com.ping.ojbackendmodel.model.entity.QuestionSubmit;
import com.ping.ojbackendquestionservice.service.QuestionService;
import com.ping.ojbackendquestionservice.service.QuestionSubmitService;
import com.ping.ojbackendserviceclient.service.QuestionFeignClient;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 该服务仅内部调用，前端不可调用
 */
@RestController("/inner")
public class QuestionInnerController implements QuestionFeignClient {

    @Resource
    private QuestionService questionService;

    @Resource
    private QuestionSubmitService questionSubmitService;

    /**
     * 根据 ID 获取题目
     *
     * @param questionId
     * @return
     */
    @Override
    @GetMapping("/get/id")
    public Question getQuestionById(@RequestParam("questionId") long questionId) {
        return questionService.getById(questionId);
    }

    /**
     * 根据 ID 获取题目提交
     *
     * @param questionSubmitId
     * @return
     */
    @Override
    @GetMapping("/question_submit/get/id")
    public QuestionSubmit getQuestionSubmitById(@RequestParam("questionSubmitId")
                                                long questionSubmitId) {
        return questionSubmitService.getById(questionSubmitId);

    }

    /**
     * 更新题目提交
     *
     * @param questionSubmit
     * @return
     */
    @Override
    @PostMapping("/question_submit/update")
    public boolean updateQuestionSubmitById(@RequestBody QuestionSubmit questionSubmit) {
        return questionSubmitService.updateById(questionSubmit);
    }
}
