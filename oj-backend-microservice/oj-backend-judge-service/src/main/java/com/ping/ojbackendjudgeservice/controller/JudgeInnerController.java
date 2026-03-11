package com.ping.ojbackendjudgeservice.controller;

import com.ping.ojbackendjudgeservice.judge.JudgeService;
import com.ping.ojbackendmodel.model.entity.QuestionSubmit;
import com.ping.ojbackendserviceclient.service.JudgeFeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 仅内部调用，前端不可调用
 */
@RestController
@RequestMapping("/inner")
public class JudgeInnerController implements JudgeFeignClient {

    @Resource
    private JudgeService judgeService;

    /**
     * 根据题目提交 id 进行判题
     *
     * @param questionSubmitId 题目提交 ID
     * @return 题目提交 - 内含判题信息
     */
    @PostMapping("/do")
    public QuestionSubmit doJudge(@RequestParam("questionSubmitId") long questionSubmitId) {
        return judgeService.doJudge(questionSubmitId);
    }
}
