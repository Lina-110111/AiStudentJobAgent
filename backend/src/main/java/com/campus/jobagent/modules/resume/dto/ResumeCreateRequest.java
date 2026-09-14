package com.campus.jobagent.modules.resume.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 简历登记请求（附件上传后登记元数据）。
 */
@Schema(description = "简历登记请求")
public record ResumeCreateRequest(
        @Schema(description = "简历标题", example = "Java后端开发-张三")
        @NotBlank(message = "简历标题不能为空")
        String title,

        @Schema(description = "目标岗位", example = "Java后端开发工程师")
        String targetJob,

        @Schema(description = "已上传文件的相对路径（先调用上传接口获得）")
        String filePath,

        @Schema(description = "原始文件名")
        String fileName,

        @Schema(description = "文件类型 pdf/docx/txt")
        String fileType,

        @Schema(description = "简历正文文本，用于 AI 诊断（未上传文件时可直接粘贴）")
        String contentText
) {
}
