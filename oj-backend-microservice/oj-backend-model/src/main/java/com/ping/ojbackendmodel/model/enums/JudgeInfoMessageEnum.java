package com.ping.ojbackendmodel.model.enums;

import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 判题信息消息枚举
 *
 * value: 用于系统内部判定的枚举值（英文）
 * text: 用于展示给用户的描述信息（中文）
 */
@Getter
public enum JudgeInfoMessageEnum {

    // ================== 成功状态 ==================
    ACCEPTED("Accepted", "成功"),

    // ================== 等待状态 ==================
    WAITING("Waiting", "等待中"),

    // ================== 错误状态 ==================
    WRONG_ANSWER("Wrong Answer", "答案错误"),
    COMPILE_ERROR("Compile Error", "编译错误"),
    RUNTIME_ERROR("Runtime Error", "运行错误"),
    TIME_LIMIT_EXCEEDED("Time Limit Exceeded", "时间超限"),
    MEMORY_LIMIT_EXCEEDED("Memory Limit Exceeded", "内存超限"),
    OUTPUT_LIMIT_EXCEEDED("Output Limit Exceeded", "输出超限"),
    PRESENTATION_ERROR("Presentation Error", "格式错误"),
    DANGEROUS_OPERATION("Dangerous Operation", "危险操作"),

    // ================== 系统错误 ==================
    SYSTEM_ERROR("System Error", "系统错误");

    /**
     * 系统内部判定的枚举值（英文，用于代码逻辑判断）
     */
    private final String value;

    /**
     * 展示给用户的描述信息（中文，用于前端展示）
     */
    private final String text;

    /**
     * 构造函数
     *
     * @param value 系统内部判定的枚举值
     * @param text 展示给用户的描述信息
     */
    JudgeInfoMessageEnum(String value, String text) {
        this.value = value;
        this.text = text;
    }

    /**
     * 获取所有系统内部值列表
     *
     * @return 系统内部值列表
     */
    public static List<String> getValues() {
        return Arrays.stream(values())
                .map(JudgeInfoMessageEnum::getValue)
                .collect(Collectors.toList());
    }

    /**
     * 获取所有展示文本列表
     *
     * @return 展示文本列表
     */
    public static List<String> getTexts() {
        return Arrays.stream(values())
                .map(JudgeInfoMessageEnum::getText)
                .collect(Collectors.toList());
    }

    /**
     * 根据系统内部值获取枚举
     *
     * @param value 系统内部值
     * @return 枚举对象，不存在返回 null
     */
    public static JudgeInfoMessageEnum getEnumByValue(String value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        return Arrays.stream(values())
                .filter(enumItem -> enumItem.value.equals(value))
                .findFirst()
                .orElse(null);
    }

    /**
     * 根据展示文本获取枚举
     *
     * @param text 展示文本
     * @return 枚举对象，不存在返回 null
     */
    public static JudgeInfoMessageEnum getEnumByText(String text) {
        if (ObjectUtils.isEmpty(text)) {
            return null;
        }
        return Arrays.stream(values())
                .filter(enumItem -> enumItem.text.equals(text))
                .findFirst()
                .orElse(null);
    }

    /**
     * 判断是否为成功状态
     *
     * @return true: 成功状态
     */
    public boolean isSuccess() {
        return this == ACCEPTED;
    }

    /**
     * 判断是否为等待状态
     *
     * @return true: 等待状态
     */
    public boolean isWaiting() {
        return this == WAITING;
    }

    /**
     * 判断是否为错误状态
     *
     * @return true: 错误状态
     */
    public boolean isError() {
        return this != ACCEPTED && this != WAITING;
    }
}