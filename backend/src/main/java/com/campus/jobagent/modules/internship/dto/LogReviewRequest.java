package com.campus.jobagent.modules.internship.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 日志批阅请求。
 */
@Schema(description = "日志批阅请求")
public record LogReviewRequest(
        String comment,

        @Schema(description = "评分 0-100")
        Integer score
) {
}
