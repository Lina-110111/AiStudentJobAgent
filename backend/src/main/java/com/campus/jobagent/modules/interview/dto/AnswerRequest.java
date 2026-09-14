package com.campus.jobagent.modules.interview.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 提交单题作答。
 */
@Schema(description = "面试作答请求")
public record AnswerRequest(
        @Schema(description = "题目序号，从 0 开始")
        @NotNull(message = "题目序号不能为空")
        Integer questionIndex,

        @NotBlank(message = "回答内容不能为空")
        String answer
) {
}
