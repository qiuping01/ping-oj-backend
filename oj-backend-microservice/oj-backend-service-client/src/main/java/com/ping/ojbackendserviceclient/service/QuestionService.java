package com.ping.ojbackendserviceclient.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ping.ojbackendmodel.model.dto.question.QuestionQueryRequest;
import com.ping.ojbackendmodel.model.entity.Question;
import com.ping.ojbackendmodel.model.vo.QuestionVO;

import javax.servlet.http.HttpServletRequest;

/**
 * 题目服务
 */
public interface QuestionService extends IService<Question> {

    /**
     * 校验题目是否合法
     *
     * @param question 题目实体
     * @param add      是否为新增题目
     */
    void validQuestion(Question question, boolean add);

    /**
     * 获取查询条件
     *
     * @param questionQueryRequest 题目查询请求
     * @return 题目查询条件
     */
    QueryWrapper<Question> getQueryWrapper(QuestionQueryRequest questionQueryRequest);

    /**
     * 获取题目封装
     *
     * @param question 题目实体
     * @param request  Http请求
     * @return 题目封装对象
     */
    QuestionVO getQuestionVO(Question question, HttpServletRequest request);

    /**
     * 分页获取题目封装
     *
     * @param questionPage 题目分页
     * @param request      Http请求
     * @return 题目封装分页
     */
    Page<QuestionVO> getQuestionVOPage(Page<Question> questionPage, HttpServletRequest request);

}
