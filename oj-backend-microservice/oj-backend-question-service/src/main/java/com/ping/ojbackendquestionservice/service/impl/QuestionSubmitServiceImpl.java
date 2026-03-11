package com.ping.ojbackendquestionservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ping.ojbackendcommon.common.ErrorCode;
import com.ping.ojbackendcommon.constant.CommonConstant;
import com.ping.ojbackendcommon.exception.BusinessException;
import com.ping.ojbackendcommon.utils.SqlUtils;
import com.ping.ojbackendmodel.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.ping.ojbackendmodel.model.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.ping.ojbackendmodel.model.entity.Question;
import com.ping.ojbackendmodel.model.entity.QuestionSubmit;
import com.ping.ojbackendmodel.model.entity.User;
import com.ping.ojbackendmodel.model.enums.QuestionSubmitLanguageEnum;
import com.ping.ojbackendmodel.model.enums.QuestionSubmitStatusEnum;
import com.ping.ojbackendmodel.model.vo.QuestionSubmitVO;
import com.ping.ojbackendquestionservice.mapper.QuestionSubmitMapper;
import com.ping.ojbackendquestionservice.service.QuestionService;
import com.ping.ojbackendquestionservice.service.QuestionSubmitService;
import com.ping.ojbackendserviceclient.service.JudgeFeignClient;
import com.ping.ojbackendserviceclient.service.UserFeignClient;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 题目提交服务
 */
@Service
public class QuestionSubmitServiceImpl extends ServiceImpl<QuestionSubmitMapper, QuestionSubmit>
        implements QuestionSubmitService {

    @Resource
    private UserFeignClient userFeignClient;

    @Resource
    private QuestionService questionService;

    @Resource
    @Lazy
    private JudgeFeignClient judgeFeignClient;

    /**
     * 执行题目提交
     *
     * @param questionSubmitAddRequest 题目提交添加请求
     * @param loginUser                登录用户
     * @return 题目提交 id
     */
    @Override
    public long doQuestionSubmit(QuestionSubmitAddRequest questionSubmitAddRequest, User loginUser) {
        // 1. 校验编程语言是否合法
        String language = questionSubmitAddRequest.getLanguage();
        QuestionSubmitLanguageEnum languageEnum = QuestionSubmitLanguageEnum.getEnumByValue(language);
        if (languageEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "编程语言不合法");
        }
        // 2. 判断实体是否存在
        Long questionId = questionSubmitAddRequest.getQuestionId();
        Question question = questionService.getById(questionId);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题目不存在");
        }
        // 3. 每个用户串行提交题目
        Long userId = loginUser.getId();
        QuestionSubmit questionSubmit = new QuestionSubmit();
        questionSubmit.setUserId(userId);
        questionSubmit.setQuestionId(questionId);
        questionSubmit.setLanguage(language);
        questionSubmit.setCode(questionSubmitAddRequest.getCode());
        // 4. 设置初始状态
        questionSubmit.setStatus(QuestionSubmitStatusEnum.WAITING.getValue());
        questionSubmit.setJudgeInfo("{}");
        boolean result = this.save(questionSubmit);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "题目提交失败");
        }
        // todo 执行判题服务
        // 异步执行判题服务
        Long questionSubmitId = questionSubmit.getId();
        CompletableFuture.runAsync(() -> {
            judgeFeignClient.doJudge(questionSubmitId);
        });
        // 5. 返回提交结果
        return questionSubmitId;
    }

    /**
     * 获取查询条件
     *
     * @param questionSubmitQueryRequest 题目提交查询请求
     * @return 查询条件包装类
     */
    @Override
    public QueryWrapper<QuestionSubmit> getQueryWrapper(QuestionSubmitQueryRequest questionSubmitQueryRequest) {
        QueryWrapper<QuestionSubmit> queryWrapper = new QueryWrapper<>();
        if (questionSubmitQueryRequest == null) {
            return queryWrapper;
        }
        String language = questionSubmitQueryRequest.getLanguage();
        Integer status = questionSubmitQueryRequest.getStatus();
        Long questionId = questionSubmitQueryRequest.getQuestionId();
        Long userId = questionSubmitQueryRequest.getUserId();
        String sortField = questionSubmitQueryRequest.getSortField();
        String sortOrder = questionSubmitQueryRequest.getSortOrder();
        // 拼接查询条件
        queryWrapper.eq(StringUtils.isNotBlank(language), "language", language);
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionId), "questionId", questionId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(QuestionSubmitStatusEnum.getEnumByValue(status) != null, "status", status);
        // 只查询没有删除的题目提交
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    /**
     * 获取题目提交包装类
     *
     * @param questionSubmit 题目提交
     * @param loginUser      登录用户
     * @return 题目提交包装类
     */
    @Override
    public QuestionSubmitVO getQuestionSubmitVO(QuestionSubmit questionSubmit, User loginUser) {
        QuestionSubmitVO questionSubmitVO = QuestionSubmitVO.objToVo(questionSubmit);
        Long userId = loginUser.getId();
        // 脱敏：仅本人和管理员能看见自己（提交 userId 和登录用户 id 不同）提交的代码
        if (!userId.equals(questionSubmit.getUserId()) && !userFeignClient.isAdmin(loginUser)) {
            questionSubmitVO.setCode(null);
        }
        return questionSubmitVO;
    }

    /**
     * 获取题目提交分页信息 - 脱敏
     *
     * @param questionSubmitPage 题目提交分页信息
     * @param loginUser          登录用户
     * @return 题目提交分页信息 - 脱敏
     */
    @Override
    public Page<QuestionSubmitVO> getQuestionSubmitVOPage(Page<QuestionSubmit> questionSubmitPage, User loginUser) {
        List<QuestionSubmit> questionSubmitList = questionSubmitPage.getRecords();
        Page<QuestionSubmitVO> questionSubmitVOPage =
                new Page<>(questionSubmitPage.getCurrent(), questionSubmitPage.getSize(), questionSubmitPage.getTotal());
        if (CollectionUtils.isEmpty(questionSubmitList)) {
            return questionSubmitVOPage;
        }
        List<QuestionSubmitVO> questionSubmitVOList = questionSubmitList.stream()
                .map(questionSubmit -> getQuestionSubmitVO(questionSubmit, loginUser))
                .collect(Collectors.toList());
        questionSubmitVOPage.setRecords(questionSubmitVOList);
        return questionSubmitVOPage;
    }
}




