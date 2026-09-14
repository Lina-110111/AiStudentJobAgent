package com.campus.jobagent.modules.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * 完善个人档案请求（对应“学生完善个人档案”需求）。
 */
@Schema(description = "个人档案更新请求")
public record ProfileUpdateRequest(
        @Size(max = 32, message = "姓名长度不能超过32个字符")
        String realName,

        String phone,

        @Email(message = "邮箱格式不正确")
        String email,

        String avatar,

        String college,

        String major,

        String grade,

        String education,

        @Size(max = 255, message = "技能标签过长")
        String skills,

        @Size(max = 255, message = "求职意向过长")
        String jobIntention
) {
}
