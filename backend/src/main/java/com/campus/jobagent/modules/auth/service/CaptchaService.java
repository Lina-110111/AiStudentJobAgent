package com.campus.jobagent.modules.auth.service;

import com.campus.jobagent.common.api.ResultCode;
import com.campus.jobagent.common.exception.BizException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wf.captcha.SpecCaptcha;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 验证码服务：两种防机器人验证码。
 *
 * <p>1. 图形数字验证码：easy-captcha 生成，答案内存存储，2 分钟过期。
 * <p>2. 顺序点选验证码：演示简化版，内存存正确点击下标序列。
 * <p>两种验证码均为一次性消费：无论校验成功或失败都删除记录，防重放。
 * <p>注：生产环境应使用 Redis 存储，此处本地演示用 ConcurrentHashMap 替代。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CaptchaService {

    private static final Duration EXPIRE = Duration.ofMinutes(2);

    private final ObjectMapper objectMapper;

    // 内存存储验证码：key -> [答案, 过期时间戳]
    private final Map<String, Object[]> captchaStore = new ConcurrentHashMap<>();

    /** 第一种：图形数字验证码。返回 captchaId + base64 图片。 */
    public Map<String, Object> image() {
        // 宽130、高48、6位字符
        SpecCaptcha captcha = new SpecCaptcha(130, 48, 6);
        String captchaId = UUID.randomUUID().toString();
        String code = captcha.text().toLowerCase();
        // 存内存，2分钟过期
        captchaStore.put("captcha:" + captchaId, new Object[]{code, Instant.now().plus(EXPIRE).toEpochMilli()});
        return Map.of(
                "captchaId", captchaId,
                "image", captcha.toBase64()
        );
    }

    /** 第二种：顺序点选验证码。返回 captchaId + 图片名列表，内存存正确点击顺序。 */
    public Map<String, Object> click() {
        String captchaId = UUID.randomUUID().toString();
        // 正确点击顺序：[2,0,3,1] —— 依次点击下标 2→0→3→1
        List<Integer> answer = Arrays.asList(2, 0, 3, 1);
        try {
            String json = objectMapper.writeValueAsString(answer);
            captchaStore.put("click:" + captchaId, new Object[]{json, Instant.now().plus(EXPIRE).toEpochMilli()});
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("序列化点选验证码答案失败", e);
        }
        // 演示使用图片文件名，前端自行准备四张图片
        List<String> images = Arrays.asList("cat.jpg", "dog.jpg", "bird.jpg", "fish.jpg");
        return Map.of(
                "captchaId", captchaId,
                "images", images
        );
    }

    /** 校验图形验证码，一次性使用。 */
    public boolean verifyImage(String captchaId, String inputCode) {
        if (captchaId == null || inputCode == null) {
            return false;
        }
        String key = "captcha:" + captchaId;
        Object[] entry = captchaStore.remove(key);
        if (entry == null) {
            return false;
        }
        // 检查是否过期
        long expireAt = (Long) entry[1];
        if (Instant.now().toEpochMilli() > expireAt) {
            return false;
        }
        String saved = (String) entry[0];
        boolean pass = saved.equalsIgnoreCase(inputCode.trim());
        return pass;
    }

    /** 校验顺序点选验证码，一次性使用。 */
    public boolean verifyClick(String captchaId, List<Integer> userClickOrder) {
        if (captchaId == null || userClickOrder == null) {
            return false;
        }
        String key = "click:" + captchaId;
        Object[] entry = captchaStore.remove(key);
        if (entry == null) {
            return false;
        }
        // 检查是否过期
        long expireAt = (Long) entry[1];
        if (Instant.now().toEpochMilli() > expireAt) {
            return false;
        }
        String jsonAnswer = (String) entry[0];
        List<Integer> correct;
        try {
            correct = objectMapper.readValue(jsonAnswer, new TypeReference<List<Integer>>() {
            });
        } catch (Exception e) {
            return false;
        }
        return correct.equals(userClickOrder);
    }

    /** 登录前强制校验两种验证码，任一失败抛业务异常。 */
    public void verifyBeforeLogin(String imageCaptchaId, String captchaCode,
                                  String clickCaptchaId, List<Integer> clickCaptcha) {
        if (!verifyImage(imageCaptchaId, captchaCode)) {
            throw BizException.of(ResultCode.CAPTCHA_ERROR, "图形验证码错误或已过期");
        }
        if (!verifyClick(clickCaptchaId, clickCaptcha)) {
            throw BizException.of(ResultCode.CAPTCHA_ERROR, "图片点选顺序错误");
        }
    }
}
