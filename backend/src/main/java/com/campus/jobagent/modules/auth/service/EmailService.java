package com.campus.jobagent.modules.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    // 内存存验证码
    private final Map<String, EmailCodeInfo> codeStore = new ConcurrentHashMap<>();
    private static final long EXPIRE_MILLIS = 5 * 60 * 1000; // 5分钟

    private static class EmailCodeInfo {
        String code;
        long expireTime;

        EmailCodeInfo(String code, long expireTime) {
            this.code = code;
            this.expireTime = expireTime;
        }
    }

    /**
     * 发送邮箱验证码
     */
    public void sendEmailCode(String email) {
        // 生成6位随机验证码
        String code = String.format("%06d", new Random().nextInt(1000000));

        // 存到内存
        codeStore.put(email, new EmailCodeInfo(code, System.currentTimeMillis() + EXPIRE_MILLIS));

        // 发邮件
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("1183385627@qq.com"); // 你的QQ邮箱
        message.setTo(email);
        message.setSubject("AiStudentJobAgent 验证码");
        message.setText("您的验证码是：" + code + "，5分钟内有效。");

        mailSender.send(message);
        log.info("邮箱验证码已发送至: {}", email);
    }

    /**
     * 校验验证码
     */
    public boolean validateCode(String email, String code) {
        EmailCodeInfo info = codeStore.get(email);
        if (info == null) {
            return false;
        }
        if (System.currentTimeMillis() > info.expireTime) {
            codeStore.remove(email);
            return false;
        }
        boolean valid = info.code.equals(code);
        if (valid) {
            codeStore.remove(email);
        }
        return valid;
    }
}
