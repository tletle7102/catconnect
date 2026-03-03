package com.matchhub.catconnect.global.util.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtProvider.class);
    private final String secret = "2025-prototype-sample-secret-key-1234567890";
    private final long expirationTime = 1000 * 60 * 60; // 1시간
    private final long extendedExpirationTime = 1000 * 60 * 60 * 24 *7; // 7일 (로그인 상태 유지)

    public String generateToken(String username, String role) {
        return generateToken(username, role, false);
    }

    public String generateToken(String username, String role, boolean stayLoggedIn) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);

        long expiration = stayLoggedIn ? extendedExpirationTime : expirationTime;

        String token = Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .compact();

        logger.debug("JWT 토큰 생성: username={}, role={}, stayLoggedIn={}", username, role, stayLoggedIn);
        return token;
    }

    public long getExpirationSeconds(boolean stayLoggedIn) {
        return stayLoggedIn ? extendedExpirationTime / 1000 : expirationTime / 1000;
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public String getRoleFromToken(String token) {
        return  Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .get("role", String.class);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseSignedClaims(token);
            logger.debug("JWT 토큰 유효성 검증 성공: token={}", token.substring(0, Math.min(token.length(), 20)) + "...");
            return true;
        } catch (Exception e) {
            logger.error("JWT 토큰 유효성 검증 실패: error={}, token={}", e.getMessage(), token.substring(0, Math.min(token.length(), 20)) + "...");
            return false;
        }
    }
}
