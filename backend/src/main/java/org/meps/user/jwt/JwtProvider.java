package org.meps.user.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

/**
 * JWT 액세스 토큰 생성·검증
 */
@Component
public class JwtProvider {

    @Value("${jwt.secret}")
    private String secret;

    /** 만료 시간(ms) */
    @Value("${jwt.expiration}")
    private long expirationMs;

    private Key key;

    @PostConstruct
    void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /** 액세스 토큰 발급 */
    public String createToken(Integer userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key)
                .compact();
    }

    /** 만료 시간(초) — 응답의 expiresIn용 */
    public int getExpiresInSeconds() {
        return (int) (expirationMs / 1000);
    }
}