package com.ping.oj.judge;

import cn.hutool.json.JSONUtil;
import com.ping.oj.common.ErrorCode;
import com.ping.oj.exception.BusinessException;
import com.ping.oj.judge.codesandbox.CodeSandbox;
import com.ping.oj.judge.codesandbox.CodeSandboxFactory;
import com.ping.oj.judge.codesandbox.CodeSandboxProxy;
import com.ping.oj.judge.codesandbox.model.ExecuteCodeRequest;
import com.ping.oj.judge.codesandbox.model.ExecuteCodeResponse;
import com.ping.oj.judge.strategy.DefaultJudgeStrategy;
import com.ping.oj.judge.strategy.JudgeContext;
import com.ping.oj.judge.strategy.JudgeStrategy;
import com.ping.oj.model.dto.question.JudgeCase;
import com.ping.oj.model.dto.questionsubmit.JudgeInfo;
import com.ping.oj.model.entity.Question;
import com.ping.oj.model.entity.QuestionSubmit;
import com.ping.oj.model.enums.QuestionSubmitStatusEnum;
import com.ping.oj.service.QuestionService;
import com.ping.oj.service.QuestionSubmitService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 判题服务实现
 */
@Service
public class JudgeServiceImpl implements JudgeService {

    @Resource
    private QuestionSubmitService questionSubmitService;

    @Resource
    private QuestionService questionService;

    @Value("${codesandbox.type:example}")
    private String type;

    /**
     * 根据题号进行判题
     *
     * @param questionSubmitId 题目提交 ID
     * @return 题目提交 - 内含判题信息
     */
    @Override
    public QuestionSubmit doJudge(long questionSubmitId) {
        // 1. 传入题目的提交 id，获取到对应的题目、提交信息（包含代码、编程语言等）
        if (questionSubmitId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QuestionSubmit questionSubmit = questionSubmitService.getById(questionSubmitId);
        if (questionSubmit == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题目提交不存在");
        }
        Long questionId = questionSubmit.getQuestionId();
        Question question = questionService.getById(questionId);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题目不存在");
        }
        // 如果题目的提交状态不为等待中，就不用重复执行了
        Integer status = questionSubmit.getStatus();
        if (!QuestionSubmitStatusEnum.WAITING.getValue().equals(status)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "题目正在判题中");
        }
        // 更改判题（题目提交）的状态为 “判题中”，及时更新数据库，防止重复执行
        QuestionSubmit questionSubmitUpdate = new QuestionSubmit();
        questionSubmitUpdate.setId(questionSubmitId);
        questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.RUNNING.getValue());
        boolean result = questionSubmitService.updateById(questionSubmitUpdate);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目状态更新错误");
        }
        // 2. 调用沙箱，获取到执行结果
        String language = questionSubmit.getLanguage();
        String code = questionSubmit.getCode();
        CodeSandbox codeSandbox = CodeSandboxFactory.newInstance(type);
        CodeSandboxProxy codeSandboxProxy = new CodeSandboxProxy(codeSandbox);
        // 获取题目的输入用例给沙箱
        String judgeCaseStr = question.getJudgeCase();
        List<JudgeCase> judgeCaseList = JSONUtil.toList(judgeCaseStr, JudgeCase.class);
        List<String> inputList = judgeCaseList.stream()
                .map(JudgeCase::getInput)
                .collect(Collectors.toList());
        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
                .code(code)
                .language(language)
                .inputList(inputList)
                .build();
        ExecuteCodeResponse executeCodeResponse = codeSandboxProxy.executeCode(executeCodeRequest);
        // 3. 根据沙箱的执行结果，设置题目的判题状态和信息
        JudgeContext judgeContext = new JudgeContext();
        judgeContext.setJudgeInfo(executeCodeResponse.getJudgeInfo());
        judgeContext.setInputList(inputList);
        judgeContext.setOutputList(executeCodeResponse.getOutputList());
        judgeContext.setJudgeCaseList(judgeCaseList);
        judgeContext.setQuestion(question);
        // 使用默认判题策略
        JudgeStrategy judgeStrategy = new DefaultJudgeStrategy();
        JudgeInfo judgeInfoResponse = judgeStrategy.doJudge(judgeContext);
        // 4. 更新数据库中题目的判题状态和信息
        questionSubmitUpdate = new QuestionSubmit();
        questionSubmitUpdate.setId(questionSubmitId);
        questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.SUCCEED.getValue());
        questionSubmitUpdate.setJudgeInfo(JSONUtil.toJsonStr(judgeInfoResponse));
        result = questionSubmitService.updateById(questionSubmitUpdate);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目状态更新错误");
        }
        return questionSubmitService.getById(questionSubmitId);
    }
}
