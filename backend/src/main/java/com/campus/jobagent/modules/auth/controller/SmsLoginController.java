package com.campus.jobagent.modules.auth.controller;

import com.campus.jobagent.common.api.Result;
import com.campus.jobagent.modules.auth.service.AuthService;
import com.campus.jobagent.modules.auth.service.CaptchaService;
import com.campus.jobagent.modules.auth.service.SmsService;
import com.campus.jobagent.modules.user.entity.SysUser;
import com.campus.jobagent.modules.user.mapper.SysUserMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth/sms")
@RequiredArgsConstructor
public class SmsLoginController {

    private final SmsService smsService;
    private final AuthService authService;
    private final SysUserMapper sysUserMapper;
    private final CaptchaService captchaService;

    /**
     * 发送短信验证码
     */
    @PostMapping("/send-code")
    public Result<Void> sendCode(@RequestBody SendCodeRequest request) {
        smsService.sendSmsCode(request.getPhone());
        return Result.ok();
    }

    /**
     * 短信验证码登录
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> smsLogin(@RequestBody SmsLoginRequest request) {
        // 阶段4：短信登录同样强制校验两种验证码，失败抛出业务异常，不进入登录逻辑
        captchaService.verifyBeforeLogin(
                request.getImageCaptchaId(), request.getCaptchaCode(),
                request.getClickCaptchaId(), request.getClickCaptcha());

        // 1. 校验短信验证码
        boolean valid = smsService.validateCode(request.getPhone(), request.getCode());
        if (!valid) {
            return Result.fail(40001, "验证码错误或已过期");
        }

        // 2. 根据手机号查用户
        SysUser user = sysUserMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getPhone, request.getPhone())
                .eq(SysUser::getDeleted, 0)
        );

        if (user == null) {
            return Result.fail(40002, "该手机号未注册");
        }

        // 3. 生成JWT token
        Map<String, Object> tokenInfo = authService.generateTokenForUser(user);
        return Result.ok(tokenInfo);
    }

    @Data
    public static class SendCodeRequest {
        private String phone;
    }

    @Data
    public static class SmsLoginRequest {
        private String phone;
        private String code;
        private String imageCaptchaId;
        private String captchaCode;
        private String clickCaptchaId;
        private List<Integer> clickCaptcha;
    }
}
