package com.ping.ojbackendjudgeservice.judge;

import cn.hutool.json.JSONUtil;
import com.ping.ojbackendcommon.common.ErrorCode;
import com.ping.ojbackendcommon.exception.BusinessException;
import com.ping.ojbackendjudgeservice.judge.codesandbox.CodeSandbox;
import com.ping.ojbackendjudgeservice.judge.codesandbox.CodeSandboxFactory;
import com.ping.ojbackendjudgeservice.judge.codesandbox.CodeSandboxProxy;
import com.ping.ojbackendjudgeservice.judge.strategy.JudgeContext;
import com.ping.ojbackendmodel.model.codesandbox.ExecuteCodeRequest;
import com.ping.ojbackendmodel.model.codesandbox.ExecuteCodeResponse;
import com.ping.ojbackendmodel.model.codesandbox.JudgeInfo;
import com.ping.ojbackendmodel.model.dto.question.JudgeCase;
import com.ping.ojbackendmodel.model.entity.Question;
import com.ping.ojbackendmodel.model.entity.QuestionSubmit;
import com.ping.ojbackendmodel.model.enums.QuestionSubmitStatusEnum;
import com.ping.ojbackendserviceclient.service.QuestionFeignClient;
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
    private QuestionFeignClient questionFeignClient;

    @Value("${codesandbox.type:example}")
    private String type;
    
    @Resource
    private JudgeManager judgeManager;

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
        QuestionSubmit questionSubmit = questionFeignClient.getQuestionSubmitById(questionSubmitId);
        if (questionSubmit == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题目提交不存在");
        }
        Long questionId = questionSubmit.getQuestionId();
        Question question = questionFeignClient.getQuestionById(questionId);
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
        boolean result = questionFeignClient.updateQuestionSubmitById(questionSubmitUpdate);
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
        judgeContext.setQuestionSubmit(questionSubmit);
        // 使用默认判题策略
        JudgeInfo judgeInfoResponse = judgeManager.doJudge(judgeContext);
        // 4. 更新数据库中题目的判题状态和信息
        questionSubmitUpdate = new QuestionSubmit();
        questionSubmitUpdate.setId(questionSubmitId);
        questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.SUCCEED.getValue());
        questionSubmitUpdate.setJudgeInfo(JSONUtil.toJsonStr(judgeInfoResponse));
        result = questionFeignClient.updateQuestionSubmitById(questionSubmitUpdate);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目状态更新错误");
        }
        return questionFeignClient.getQuestionSubmitById(questionSubmitId);
    }
}
