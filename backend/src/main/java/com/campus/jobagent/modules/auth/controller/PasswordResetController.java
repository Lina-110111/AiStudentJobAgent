package com.campus.jobagent.modules.auth.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.campus.jobagent.common.api.Result;
import com.campus.jobagent.common.api.ResultCode;
import com.campus.jobagent.common.exception.BizException;
import com.campus.jobagent.modules.auth.service.CaptchaService;
import com.campus.jobagent.modules.auth.service.EmailService;
import com.campus.jobagent.modules.auth.service.SmsService;
import com.campus.jobagent.modules.user.entity.SysUser;
import com.campus.jobagent.modules.user.mapper.SysUserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 忘记密码：通过注册时绑定的手机号或邮箱接收验证码，重置登录密码。
 *
 * <p>安全设计：
 * <p>1. 发送验证码前强制校验图形验证码，防止短信/邮件接口被刷量；
 * <p>2. 校验证码时一次性消费，防止同一验证码重复使用；
 * <p>3. 新密码统一走 BCrypt 加密后落库，不覆盖其它用户信息。
 */
@Slf4j
@Tag(name = "密码重置", description = "忘记密码：通过手机号或邮箱验证码重置登录密码")
@RestController
@RequestMapping("/api/auth/password")
@RequiredArgsConstructor
public class PasswordResetController {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final int PASSWORD_MIN = 6;
    private static final int PASSWORD_MAX = 32;

    private final SysUserMapper sysUserMapper;
    private final CaptchaService captchaService;
    private final SmsService smsService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Operation(summary = "忘记密码 - 发送验证码")
    @PostMapping("/send-code")
    public Result<Map<String, Object>> sendCode(@RequestBody SendCodeRequest request) {
        String account = request.account() == null ? "" : request.account().trim();

        // 1. 先校验图形验证码，避免短信/邮件接口被恶意刷量
        if (!captchaService.verifyImage(request.captchaId(), request.captchaCode())) {
            throw BizException.of(ResultCode.CAPTCHA_ERROR, "图形验证码错误或已过期");
        }

        // 2. 识别账号类型，并确认该账号确实存在（否则不发码）
        Account resolved = resolve(account);
        if (resolved.user() == null) {
            throw BizException.of(ResultCode.USER_NOT_FOUND,
                    resolved.phone() ? "该手机号未注册" : "该邮箱未注册");
        }

        // 3. 按渠道下发验证码
        if (resolved.phone()) {
            smsService.sendSmsCode(resolved.value());
        } else {
            emailService.sendEmailCode(resolved.value());
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("channel", resolved.phone() ? "SMS" : "EMAIL");
        data.put("maskedAccount", mask(resolved.value()));
        log.info("忘记密码验证码已发送: channel={}, account={}", data.get("channel"), data.get("maskedAccount"));
        return Result.ok("验证码已发送", data);
    }

    @Operation(summary = "忘记密码 - 重置密码")
    @PostMapping("/reset")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> reset(@RequestBody ResetRequest request) {
        String account = request.account() == null ? "" : request.account().trim();
        String code = request.code() == null ? "" : request.code().trim();
        String newPassword = request.newPassword() == null ? "" : request.newPassword().trim();

        if (newPassword.length() < PASSWORD_MIN || newPassword.length() > PASSWORD_MAX) {
            throw BizException.of(ResultCode.PARAM_INVALID, "新密码长度需在 6-32 位之间");
        }
        if (code.isEmpty()) {
            throw BizException.of(ResultCode.PARAM_INVALID, "请填写验证码");
        }

        Account resolved = resolve(account);
        if (resolved.user() == null) {
            throw BizException.of(ResultCode.USER_NOT_FOUND,
                    resolved.phone() ? "该手机号未注册" : "该邮箱未注册");
        }

        // 校验验证码：一次性消费，成功即失效
        boolean valid = resolved.phone()
                ? smsService.validateCode(resolved.value(), code)
                : emailService.validateCode(resolved.value(), code);
        if (!valid) {
            throw BizException.of(ResultCode.CAPTCHA_ERROR, "验证码错误或已过期");
        }

        SysUser user = resolved.user();
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdateTime(LocalDateTime.now());
        sysUserMapper.updateById(user);

        log.info("密码重置成功: username={}, channel={}", user.getUsername(),
                resolved.phone() ? "SMS" : "EMAIL");
        return Result.<Void>ok("密码重置成功，请用新密码登录", null);
    }

    /** 按手机号或邮箱定位用户，格式不合法直接报参数错误。 */
    private Account resolve(String account) {
        if (PHONE_PATTERN.matcher(account).matches()) {
            SysUser user = sysUserMapper.selectOne(Wrappers.<SysUser>lambdaQuery()
                    .eq(SysUser::getPhone, account)
                    .last("limit 1"));
            return new Account(account, true, user);
        }
        if (EMAIL_PATTERN.matcher(account).matches()) {
            SysUser user = sysUserMapper.selectOne(Wrappers.<SysUser>lambdaQuery()
                    .eq(SysUser::getEmail, account)
                    .last("limit 1"));
            return new Account(account, false, user);
        }
        throw BizException.of(ResultCode.PARAM_INVALID, "请填写正确的手机号或邮箱");
    }

    /** 打码展示，日志与响应里不出现完整手机号/邮箱。 */
    private String mask(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        if (value.contains("@")) {
            int at = value.indexOf('@');
            String name = value.substring(0, at);
            String domain = value.substring(at);
            String head = name.length() <= 1 ? name : name.substring(0, Math.min(2, name.length()));
            return head + "***" + domain;
        }
        if (value.length() >= 7) {
            return value.substring(0, 3) + "****" + value.substring(value.length() - 4);
        }
        return value;
    }

    /** 账号解析结果：原值、是否为手机号、命中的用户。 */
    private record Account(String value, boolean phone, SysUser user) {
    }

    public record SendCodeRequest(String account, String captchaId, String captchaCode) {
    }

    public record ResetRequest(String account, String code, String newPassword) {
    }
}
