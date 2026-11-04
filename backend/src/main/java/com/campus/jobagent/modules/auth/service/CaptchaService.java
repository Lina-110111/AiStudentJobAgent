package com.campus.jobagent.modules.auth.service;

import com.campus.jobagent.common.api.ResultCode;
import com.campus.jobagent.common.exception.BizException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wf.captcha.SpecCaptcha;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 验证码服务：两种防机器人验证码。
 *
 * <p>1. 图形数字验证码：easy-captcha 生成，答案存 Redis，2 分钟过期。
 * <p>2. 顺序点选验证码：演示简化版，Redis 存正确点击下标序列。
 * <p>两种验证码均为一次性消费：无论校验成功或失败都删除 Redis 记录，防重放。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CaptchaService {

    private static final Duration EXPIRE = Duration.ofMinutes(2);

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    /** 第一种：图形数字验证码。返回 captchaId + base64 图片。 */
    public Map<String, Object> image() {
        // 宽130、高48、6位字符
        SpecCaptcha captcha = new SpecCaptcha(130, 48, 6);
        String captchaId = UUID.randomUUID().toString();
        String code = captcha.text().toLowerCase();
        // redis key: captcha:{id}，2 分钟过期
        stringRedisTemplate.opsForValue().set("captcha:" + captchaId, code, EXPIRE);
        return Map.of(
                "captchaId", captchaId,
                "image", captcha.toBase64()
        );
    }

    /** 第二种：顺序点选验证码。返回 captchaId + 图片名列表，Redis 存正确点击顺序。 */
    public Map<String, Object> click() {
        String captchaId = UUID.randomUUID().toString();
        // 正确点击顺序：[2,0,3,1] —— 依次点击下标 2→0→3→1
        List<Integer> answer = Arrays.asList(2, 0, 3, 1);
        try {
            stringRedisTemplate.opsForValue().set("click:" + captchaId, objectMapper.writeValueAsString(answer), EXPIRE);
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
        // GETDEL 保证读取与删除原子完成，避免并发请求重复消费同一个验证码。
        String saved = stringRedisTemplate.opsForValue().getAndDelete(key);
        if (saved == null) {
            return false;
        }
        boolean pass = saved.equalsIgnoreCase(inputCode.trim());
        return pass;
    }

    /** 校验顺序点选验证码，一次性使用。 */
    public boolean verifyClick(String captchaId, List<Integer> userClickOrder) {
        if (captchaId == null || userClickOrder == null) {
            return false;
        }
        String key = "click:" + captchaId;
        String jsonAnswer = stringRedisTemplate.opsForValue().getAndDelete(key);
        if (jsonAnswer == null) {
            return false;
        }
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
