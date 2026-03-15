package com.ping.oj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ping.oj.common.ErrorCode;
import com.ping.oj.constant.CommonConstant;
import com.ping.oj.constant.UserConstant;
import com.ping.oj.exception.BusinessException;
import com.ping.oj.judge.JudgeService;
import com.ping.oj.mapper.QuestionSubmitMapper;
import com.ping.oj.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.ping.oj.model.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.ping.oj.model.entity.Question;
import com.ping.oj.model.entity.QuestionSubmit;
import com.ping.oj.model.entity.User;
import com.ping.oj.model.enums.QuestionSubmitLanguageEnum;
import com.ping.oj.model.enums.QuestionSubmitStatusEnum;
import com.ping.oj.model.vo.LoginUserVO;
import com.ping.oj.model.vo.QuestionSubmitVO;
import com.ping.oj.model.vo.QuestionVO;
import com.ping.oj.model.vo.UserVO;
import com.ping.oj.service.QuestionService;
import com.ping.oj.service.QuestionSubmitService;
import com.ping.oj.service.UserService;
import com.ping.oj.utils.SqlUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 题目提交服务
 */
@Service
public class QuestionSubmitServiceImpl extends ServiceImpl<QuestionSubmitMapper, QuestionSubmit>
        implements QuestionSubmitService {

    @Resource
    private UserService userService;

    @Resource
    private QuestionService questionService;

    @Resource
    @Lazy
    private JudgeService judgeService;

    @Resource
    private RedissonClient redissonClient;

    @Value("${judge.user.max-concurrent}")
    private int defaultMaxConcurrent;

    @Value("${judge.user.rate-limit}")
    private int defaultRateLimit;

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
        Long userId = loginUser.getId();
        // a. 限制单个用户的提交频率
        int userRateLimit = getUserRateLimit(loginUser);
        String rateKey = "user:rate:" + userId;
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(rateKey);
        // 设置令牌桶：速率 = userRateLimit/60 个/秒，容量 = userRateLimit（突发可消耗全部令牌）
        rateLimiter.trySetRate(RateType.OVERALL, userRateLimit, 1, RateIntervalUnit.MINUTES);
        if (!rateLimiter.tryAcquire(1)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "提交过于频繁，请稍后再试");
        }
        // b. 限制单个用户的并发提交数
        int userMaxConcurrent = getUserMaxConcurrent(loginUser);
        String concurrentKey = "user:concurrent:" + userId;
        RAtomicLong concurrentCounter = redissonClient.getAtomicLong(concurrentKey);
        // 使用分布式锁保证检查和设置的原子性
        RLock lock = redissonClient.getLock("lock:concurrent:" + userId);
        try {
            lock.lock(5, TimeUnit.SECONDS);   // 尝试加锁，最多等待5秒
            long current = concurrentCounter.get();
            if (current >= userMaxConcurrent) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "您的提交任务过多，请稍后重试");
            }
            // 原子增加并发数
            concurrentCounter.set(current + 1);
            // 设置过期时间（如10分钟），防止服务宕机导致计数器无法释放
            concurrentCounter.expire(10, TimeUnit.MINUTES);
        } finally {
            lock.unlock();
        }

        // 3. 每个用户串行提交题目
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
            // 保存失败，需要回滚并发数
            concurrentCounter.decrementAndGet();
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "题目提交失败");
        }
        // 5. 更新题目提交数
        questionService.lambdaUpdate()
                .eq(Question::getId, question.getId())
                .set(Question::getSubmitNum, question.getSubmitNum() + 1)
                .update();
        // todo 执行判题服务
        // 异步执行判题服务
        Long questionSubmitId = questionSubmit.getId();
        CompletableFuture.runAsync(() -> {
            try {
                judgeService.doJudge(questionSubmitId);
            } finally {
                // 判题结束（无论成功或失败）必须减少并发数
                concurrentCounter.decrementAndGet();
            }
        });
        // 5. 返回提交结果
        return questionSubmitId;
    }

    /**
     * 获取用户允许的最大并发提交数（示例：管理员用户 5，普通用户 2）
     */
    private int getUserMaxConcurrent(User loginUser) {
        // 实际可从数据库用户表中获取角色字段，或从配置中心读取
        if (loginUser.getUserRole().equals(UserConstant.ADMIN_ROLE)) {
            return 5;
        }
        return defaultMaxConcurrent;
    }

    /**
     * 获取用户每分钟允许的最大提交次数
     */
    private int getUserRateLimit(User loginUser) {
        if (loginUser.getUserRole().equals(UserConstant.ADMIN_ROLE)) {
            return 10;
        }
        return defaultRateLimit;
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
        if (!userId.equals(questionSubmit.getUserId()) && !userService.isAdmin(loginUser)) {
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
        // 封装用户信息
        Set<Long> userIdSet = questionSubmitList.stream()
                .map(QuestionSubmit::getUserId)
                .filter(Objects::nonNull)  // 过滤null值
                .collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet)
                .stream()
                .collect(Collectors.groupingBy(User::getId));
        questionSubmitVOList.forEach(questionSubmitVO -> {
            Long userId = questionSubmitVO.getUserId();
            User user = null;
            UserVO userVO = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
                userVO = userService.getUserVO(user);
            }
            questionSubmitVO.setUserVO(userVO);
        });
        // 封装题目信息
        Set<Long> questionIdSet = questionSubmitList.stream()
                .map(QuestionSubmit::getQuestionId)
                .filter(Objects::nonNull)  // 过滤null值
                .collect(Collectors.toSet());
        Map<Long, List<Question>> questionIdQuestionListMap = questionService.listByIds(questionIdSet)
                .stream()
                .collect(Collectors.groupingBy(Question::getId));
        questionSubmitVOList.forEach(questionSubmitVO -> {
            Long questionId = questionSubmitVO.getQuestionId();
            Question question = null;
            QuestionVO questionVO = null;
            if (questionIdQuestionListMap.containsKey(questionId)) {
                question = questionIdQuestionListMap.get(questionId).get(0);
                questionVO = questionService.getQuestionVO(question, null);
            }
            questionSubmitVO.setQuestionVO(questionVO);
        });
        questionSubmitVOPage.setRecords(questionSubmitVOList);
        return questionSubmitVOPage;
    }
}




