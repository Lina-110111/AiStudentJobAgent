package com.campus.jobagent.modules.auth.controller;

import com.campus.jobagent.common.api.Result;
import com.campus.jobagent.common.api.ResultCode;
import com.campus.jobagent.modules.auth.service.AuthService;
import com.campus.jobagent.modules.auth.service.CaptchaService;
import com.campus.jobagent.modules.auth.service.EmailService;
import com.campus.jobagent.modules.user.entity.SysUser;
import com.campus.jobagent.modules.user.mapper.SysUserMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth/email")
@RequiredArgsConstructor
public class EmailLoginController {

    private final EmailService emailService;
    private final AuthService authService;
    private final SysUserMapper sysUserMapper;
    private final CaptchaService captchaService;

    /**
     * 发送邮箱验证码（先校验图形验证码，防止邮件轰炸）
     */
    @PostMapping("/send-code")
    public Result<Void> sendCode(@RequestBody SendEmailCodeRequest request) {
        // 阶段4：发送邮箱码前先校验图形验证码，一次性消费
        if (!captchaService.verifyImage(request.getCaptchaId(), request.getCaptchaCode())) {
            return Result.fail(ResultCode.CAPTCHA_ERROR, "图形验证码错误或已过期");
        }
        emailService.sendEmailCode(request.getEmail());
        return Result.ok();
    }

    /**
     * 邮箱验证码登录
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> emailLogin(@RequestBody EmailLoginRequest request) {
        // 阶段4：邮箱登录同样强制校验图形验证码，失败直接返回，不进入登录逻辑
        if (!captchaService.verifyImage(request.getImageCaptchaId(), request.getCaptchaCode())) {
            return Result.fail(ResultCode.CAPTCHA_ERROR, "图形验证码错误或已过期");
        }

        // 1. 校验邮箱验证码
        boolean valid = emailService.validateCode(request.getEmail(), request.getCode());
        if (!valid) {
            return Result.fail(40001, "验证码错误或已过期");
        }

        // 2. 根据邮箱查用户
        SysUser user = sysUserMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getEmail, request.getEmail())
                .eq(SysUser::getDeleted, 0)
        );

        if (user == null) {
            return Result.fail(40002, "该邮箱未注册");
        }

        // 3. 生成JWT token
        Map<String, Object> tokenInfo = authService.generateTokenForUser(user);
        return Result.ok(tokenInfo);
    }

    @Data
    public static class SendEmailCodeRequest {
        private String email;
        private String captchaId;
        private String captchaCode;
    }

    @Data
    public static class EmailLoginRequest {
        private String email;
        private String code;
        private String imageCaptchaId;
        private String captchaCode;
    }
}
