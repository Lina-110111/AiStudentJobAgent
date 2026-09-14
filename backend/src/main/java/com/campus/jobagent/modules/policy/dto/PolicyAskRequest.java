package com.campus.jobagent.modules.policy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 政策问答请求。
 */
@Schema(description = "政策问答请求")
public record PolicyAskRequest(
        @Schema(description = "学生提问", example = "应届毕业生到基层就业有补贴吗？")
        @NotBlank(message = "提问内容不能为空")
        String question
) {
}
