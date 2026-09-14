package com.campus.jobagent.modules.internship.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

/**
 * 提交实习日志。
 */
@Schema(description = "实习日志提交请求")
public record InternshipLogRequest(
        @Schema(description = "第几周")
        Integer weekNo,

        LocalDate logDate,

        @NotBlank(message = "日志内容不能为空")
        String content
) {
}
