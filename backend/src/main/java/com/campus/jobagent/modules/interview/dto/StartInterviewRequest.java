package com.campus.jobagent.modules.interview.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 开始面试训练。
 */
@Schema(description = "开始面试训练请求")
public record StartInterviewRequest(
        @Schema(description = "场景类别：技术岗 / 管理岗 / 综合素质", example = "技术岗")
        String category,

        @Schema(description = "难度：EASY / NORMAL / HARD", example = "NORMAL")
        String difficulty
) {
}
