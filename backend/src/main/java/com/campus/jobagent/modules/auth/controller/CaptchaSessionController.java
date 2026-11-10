package com.campus.jobagent.modules.auth.controller;

import com.campus.jobagent.common.api.Result;
import com.campus.jobagent.modules.auth.service.CaptchaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 答辩演示接口：只读查看当前 JVM 内存中的验证码会话数据。
 *
 * <p>该接口不做任何写操作，也不消费验证码，仅用于向答辩老师展示"会话数据存放在内存"。
 * <p>由 app.demo.captcha-peek 开关控制，置为 false 时整个控制器不再注册。
 */
@Tag(name = "验证码会话（演示）", description = "只读查看内存中的验证码记录")
@RestController
@RequestMapping("/api/captcha")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.demo.captcha-peek", havingValue = "true", matchIfMissing = true)
public class CaptchaSessionController {

    private final CaptchaService captchaService;

    @Operation(summary = "查看内存中的验证码会话（只读）")
    @GetMapping("/session")
    public Result<Map<String, Object>> session() {
        List<Map<String, Object>> entries = captchaService.snapshot();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("storeType", "ConcurrentHashMap（JVM 堆内存，非 Redis）");
        data.put("total", entries.size());
        data.put("ttlSeconds", captchaService.ttlSeconds());
        data.put("rule", "一次性消费：校验成功或失败都会立即删除；超过有效期后校验一律不通过");
        data.put("entries", entries);
        return Result.ok(data);
    }
}
