package com.campus.jobagent.security;

import com.campus.jobagent.common.constant.SecurityConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 令牌工具：签发与校验。
 *
 * <p>采用无状态令牌（不落库），令牌中携带 userId、role 两个业务声明。
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long expireMillis;
    private final String issuer;

    public JwtTokenProvider(@Value("${app.jwt.secret}") String secret,
                            @Value("${app.jwt.expire-minutes:720}") long expireMinutes,
                            @Value("${app.jwt.issuer:job-agent}") String issuer) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expireMillis = expireMinutes * 60_000L;
        this.issuer = issuer;
    }

    /** 签发令牌。 */
    public String generateToken(LoginUser loginUser) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expireMillis);
        return Jwts.builder()
                .issuer(issuer)
                .subject(loginUser.getUsername())
                .claim(SecurityConstants.CLAIM_USER_ID, loginUser.getUserId())
                .claim(SecurityConstants.CLAIM_ROLE, loginUser.getRoleCode())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    /** 解析令牌；令牌非法或过期时返回 false。 */
    public boolean validate(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("令牌校验失败：{}", e.getMessage());
            return false;
        }
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long getUserId(String token) {
        return parseClaims(token).get(SecurityConstants.CLAIM_USER_ID, Number.class).longValue();
    }

    public String getRoleCode(String token) {
        return parseClaims(token).get(SecurityConstants.CLAIM_ROLE, String.class);
    }

    public long getExpireMillis() {
        return expireMillis;
    }
}
