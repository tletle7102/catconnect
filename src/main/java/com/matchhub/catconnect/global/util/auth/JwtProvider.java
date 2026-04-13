package com.matchhub.catconnect.global.util.auth;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 토큰 생성 및 검증을 담당하는 컴포넌트
 *
 * Access Token: 짧은 유효기간 (15분, stayLoggedIn 시 7일)
 * Refresh Token: 긴 유효기간 (14일)
 */
@Component
public class JwtProvider {

	private static final Logger logger = LoggerFactory.getLogger(JwtProvider.class);
	private final String secret = "2025-prototype-sample-secret-key-1234567890";

	// Access Token 만료 시간
	private final long accessTokenExpirationTime = 1000 * 60 * 15; // 15분
	private final long extendedAccessTokenExpirationTime = 1000 * 60 * 60 * 24 * 7; // 7일 (로그인 상태 유지)

	// Refresh Token 만료 시간
	private final long refreshTokenExpirationTime = 1000 * 60 * 60 * 24 * 14; // 14일

	// 토큰 타입 구분용 클레임 키
	private static final String TOKEN_TYPE_CLAIM = "tokenType";
	private static final String ACCESS_TOKEN_TYPE = "access";
	private static final String REFRESH_TOKEN_TYPE = "refresh";

	/**
	 * Access Token 생성 (기본 15분 유효)
	 */
	public String generateAccessToken(String username, String role) {
		return generateAccessToken(username, role, false);
	}

	/**
	 * Access Token 생성 (stayLoggedIn 옵션 지원)
	 */
	public String generateAccessToken(String username, String role, boolean stayLoggedIn) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("role", role);
		claims.put(TOKEN_TYPE_CLAIM, ACCESS_TOKEN_TYPE);

		long expiration = stayLoggedIn ? extendedAccessTokenExpirationTime : accessTokenExpirationTime;

		String token = Jwts.builder()
				.claims(claims)
				.subject(username)
				.issuedAt(new Date())
				.expiration(new Date(System.currentTimeMillis() + expiration))
				.signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
				.compact();
		logger.debug("Access Token 생성: username={}, role={}, stayLoggedIn={}", username, role, stayLoggedIn);
		return token;
	}

	/**
	 * Refresh Token 생성 (14일 유효)
	 */
	public String generateRefreshToken(String username) {
		Map<String, Object> claims = new HashMap<>();
		claims.put(TOKEN_TYPE_CLAIM, REFRESH_TOKEN_TYPE);

		String token = Jwts.builder()
				.claims(claims)
				.subject(username)
				.id(java.util.UUID.randomUUID().toString()) // 토큰 고유 ID 추가 (Rotation 시 구분용)
				.issuedAt(new Date())
				.expiration(new Date(System.currentTimeMillis() + refreshTokenExpirationTime))
				.signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
				.compact();
		logger.debug("Refresh Token 생성: username={}", username);
		return token;
	}

	/**
	 * Refresh Token 만료 시각 계산 (LocalDateTime)
	 */
	public LocalDateTime getRefreshTokenExpiredAt() {
		return LocalDateTime.now().plusSeconds(refreshTokenExpirationTime / 1000);
	}

	/**
	 * Access Token 만료 시간 (초 단위)
	 */
	public long getAccessTokenExpirationSeconds(boolean stayLoggedIn) {
		return stayLoggedIn ? extendedAccessTokenExpirationTime / 1000 : accessTokenExpirationTime / 1000;
	}

	/**
	 * Refresh Token 만료 시간 (초 단위)
	 */
	public long getRefreshTokenExpirationSeconds() {
		return refreshTokenExpirationTime / 1000;
	}

	// 하위 호환성을 위한 기존 메서드 유지
	public String generateToken(String username, String role) {
		return generateAccessToken(username, role, false);
	}

	public String generateToken(String username, String role, boolean stayLoggedIn) {
		return generateAccessToken(username, role, stayLoggedIn);
	}

	public long getExpirationSeconds(boolean stayLoggedIn) {
		return getAccessTokenExpirationSeconds(stayLoggedIn);
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

	/**
	 * 토큰 유효성 검증 (만료 포함)
	 */
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

	/**
	 * 토큰 만료 여부만 확인 (서명은 유효하다고 가정)
	 */
	public boolean isTokenExpired(String token) {
		try {
			Jwts.parser()
					.verifyWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
					.build()
					.parseSignedClaims(token);
			return false;
		} catch (ExpiredJwtException e) {
			return true;
		} catch (Exception e) {
			return true;
		}
	}

	/**
	 * 만료된 토큰에서도 username 추출 (Refresh Token 검증 시 사용)
	 */
	public String getUsernameFromExpiredToken(String token) {
		try {
			return Jwts.parser()
					.verifyWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
					.build()
					.parseSignedClaims(token)
					.getPayload()
					.getSubject();
		} catch (ExpiredJwtException e) {
			// 만료된 토큰에서도 클레임은 추출 가능
			return e.getClaims().getSubject();
		}
	}
}
