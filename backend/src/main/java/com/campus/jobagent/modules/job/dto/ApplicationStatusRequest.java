package com.campus.jobagent.modules.job.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;

/**
 * 更新申请状态（企业HR / 辅导员）。
 */
@Schema(description = "申请状态变更请求")
public record ApplicationStatusRequest(
        @Pattern(regexp = "VIEWED|INTERVIEW|OFFER|REJECTED", message = "状态取值不合法")
        String status,

        String remark
) {
}
