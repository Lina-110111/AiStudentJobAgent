package com.campus.jobagent.modules.internship.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * 三方评分提交。
 */
@Schema(description = "三方评分请求")
public record ScoreRequest(
        @Schema(description = "SELF 学生自评 / MENTOR 企业导师评价 / TEACHER 校方评定")
        @NotNull(message = "评分方不能为空")
        @Pattern(regexp = "SELF|MENTOR|TEACHER", message = "评分方取值不合法")
        String role,

        @Min(value = 0, message = "评分不能小于0")
        @Max(value = 100, message = "评分不能大于100")
        @NotNull(message = "评分不能为空")
        Integer score
) {
}
