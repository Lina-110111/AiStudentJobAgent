package com.campus.jobagent.modules.auth.controller;

import com.campus.jobagent.common.api.Result;
import com.campus.jobagent.modules.auth.service.CaptchaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 验证码接口：图形数字验证码 / 顺序点选验证码。
 */
@Tag(name = "验证码", description = "图形验证码 / 顺序点选验证码")
@RestController
@RequestMapping("/api/captcha")
@RequiredArgsConstructor
public class CaptchaController {

    private final CaptchaService captchaService;

    @Operation(summary = "获取图形数字验证码")
    @GetMapping("/image")
    public Result<Map<String, Object>> image() {
        return Result.ok(captchaService.image());
    }

    @Operation(summary = "获取顺序点选验证码")
    @GetMapping("/click")
    public Result<Map<String, Object>> click() {
        return Result.ok(captchaService.click());
    }
}
