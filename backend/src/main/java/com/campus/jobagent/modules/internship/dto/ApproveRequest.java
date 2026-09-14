package com.campus.jobagent.modules.internship.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * 审批请求。
 */
@Schema(description = "流程审批请求")
public record ApproveRequest(
        @Schema(description = "true 通过 / false 驳回")
        @NotNull(message = "请选择审批结果")
        Boolean approve,

        @Schema(description = "审批意见")
        String comment
) {
}
