package com.campus.jobagent.modules.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class SmsService {

    // 用内存存验证码，key是手机号，value是验证码和过期时间
    private final Map<String, CodeInfo> codeStore = new ConcurrentHashMap<>();

    private static final long CODE_EXPIRE_MILLIS = 5 * 60 * 1000; // 5分钟

    private static class CodeInfo {
        String code;
        long expireTime;

        CodeInfo(String code, long expireTime) {
            this.code = code;
            this.expireTime = expireTime;
        }
    }

    /**
     * 发送短信验证码（先Mock，验证码直接打日志里）
     */
    public void sendSmsCode(String phone) {
        // 生成6位随机数字验证码
        String code = String.format("%06d", new Random().nextInt(1000000));
        
        // 存到内存，5分钟过期
        codeStore.put(phone, new CodeInfo(code, System.currentTimeMillis() + CODE_EXPIRE_MILLIS));
        
        // Mock：验证码直接打印到后端日志里
        log.info("========== 短信验证码发送成功 ==========");
        log.info("手机号: {}", phone);
        log.info("验证码: {}", code);
        log.info("有效期: 5分钟");
        log.info("======================================");
    }

    /**
     * 校验验证码
     */
    public boolean validateCode(String phone, String code) {
        CodeInfo info = codeStore.get(phone);
        if (info == null) {
            log.warn("验证码已过期或未发送: {}", phone);
            return false;
        }
        // 检查是否过期
        if (System.currentTimeMillis() > info.expireTime) {
            codeStore.remove(phone);
            log.warn("验证码已过期: {}", phone);
            return false;
        }
        boolean valid = info.code.equals(code);
        if (valid) {
            // 校验成功后删掉验证码，防止重复使用
            codeStore.remove(phone);
        }
        return valid;
    }
}
