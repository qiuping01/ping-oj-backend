package com.ping.ojbackendquestionservice.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ping.ojbackendmodel.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.ping.ojbackendmodel.model.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.ping.ojbackendmodel.model.entity.QuestionSubmit;
import com.ping.ojbackendmodel.model.entity.User;
import com.ping.ojbackendmodel.model.vo.QuestionSubmitVO;

/**
 * 题目提交服务
 */
public interface QuestionSubmitService extends IService<QuestionSubmit> {

    /**
     * 执行题目提交
     *
     * @param questionSubmitAddRequest 题目提交添加请求
     * @param loginUser                登录用户
     * @return 题目提交 id
     */
    long doQuestionSubmit(QuestionSubmitAddRequest questionSubmitAddRequest, User loginUser);

    /**
     * 获取查询条件
     *
     * @param questionSubmitQueryRequest 题目提交查询请求
     * @return 查询条件包装类
     */
    QueryWrapper<QuestionSubmit> getQueryWrapper(QuestionSubmitQueryRequest questionSubmitQueryRequest);

    /**
     * 获取题目提交包装类
     *
     * @param questionSubmit 题目提交
     * @param loginUser      登录用户
     * @return 题目提交包装类
     */
    QuestionSubmitVO getQuestionSubmitVO(QuestionSubmit questionSubmit, User loginUser);

    /**
     * 获取题目提交分页信息 - 脱敏
     *
     * @param questionSubmitPage 题目提交分页信息
     * @param loginUser          登录用户
     * @return 题目提交分页信息 - 脱敏
     */
    Page<QuestionSubmitVO> getQuestionSubmitVOPage(Page<QuestionSubmit> questionSubmitPage, User loginUser);
}
