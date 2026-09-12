package org.meps.user.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.LocalDateTime;
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

    /** refresh 만료 시간(ms) */
    @Value("${jwt.refreshExpiration}")
    private long refreshExpirationMs;

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

    /** 토큰에서 userId 추출. 서명 불일치·만료·형식 오류 등 유효하지 않으면 null */
    public Integer getUserId(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return Integer.valueOf(claims.getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }


    /** 리프레시 토큰 발급 */
    public String createRefreshToken(Integer userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshExpirationMs);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key)
                .compact();
    }

    /** 리프레시 토큰 만료 시각 — DB 저장용 */
    public LocalDateTime getRefreshExpiresAt() {
        return LocalDateTime.now().plusNanos(refreshExpirationMs * 1_000_000);
    }

    /** 만료 시간(초) — 응답의 expiresIn용 */
    public int getExpiresInSeconds() {
        return (int) (expirationMs / 1000);
    }
}