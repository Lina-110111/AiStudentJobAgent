package com.campus.jobagent.modules.job.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

/**
 * 岗位发布请求（企业HR / 辅导员）。
 */
@Schema(description = "岗位发布请求")
public record JobPostCreateRequest(
        @NotBlank(message = "岗位名称不能为空")
        String title,

        @NotBlank(message = "企业名称不能为空")
        String companyName,

        String jobCategory,

        @NotBlank(message = "工作城市不能为空")
        String city,

        @Min(value = 0, message = "薪资不能为负数")
        Integer salaryMin,

        @Min(value = 0, message = "薪资不能为负数")
        Integer salaryMax,

        Integer headcount,

        String educationReq,

        String description,

        @NotBlank(message = "任职要求不能为空")
        String requirement,

        LocalDate deadline
) {
}
