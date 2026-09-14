package com.campus.jobagent.modules.internship.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

/**
 * 实习申请请求。
 */
@Schema(description = "实习申请请求")
public record InternshipApplyRequest(
        @NotBlank(message = "实习单位不能为空")
        String companyName,

        @NotBlank(message = "实习岗位不能为空")
        String position,

        @Schema(description = "企业导师用户ID，可为空")
        Long mentorId,

        @Schema(description = "校内指导教师（辅导员）用户ID，可为空")
        Long teacherId,

        LocalDate startDate,

        LocalDate endDate,

        String remark
) {
}
