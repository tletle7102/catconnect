package com.matchhub.catconnect.domain.auth.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 로그인 응답 DTO
 *
 * OAuth 2.0 스펙(RFC 6749)에 따라 token_type, expires_in 포함
 * Access Token과 Refresh Token을 모두 포함
 */
@Schema(description = "로그인 응답 정보")
public class LoginResponseDTO {

	@Schema(description = "사용자 이름", example = "testuser")
	private String username;

	@Schema(description = "사용자 권한", example = "USER")
	private String role;

	@Schema(description = "Access Token (JWT)")
	private String token;

	@Schema(description = "토큰 타입 (OAuth 2.0 스펙)", example = "Bearer")
	private String tokenType = "Bearer";

	@Schema(description = "Access Token 만료 시간 (초)", example = "900")
	private Long expiresIn;

	@Schema(description = "Refresh Token (JWT)", nullable = true)
	private String refreshToken;

	@Schema(description = "인증 여부", example = "true")
	private boolean authenticated;

	public LoginResponseDTO() {
	}

	// 기존 생성자 (하위 호환성 유지)
	public LoginResponseDTO(String username, String role, String token, boolean authenticated) {
		this.username = username;
		this.role = role;
		this.token = token;
		this.authenticated = authenticated;
	}

	// Refresh Token 포함 생성자
	public LoginResponseDTO(String username, String role, String token, String refreshToken, boolean authenticated) {
		this.username = username;
		this.role = role;
		this.token = token;
		this.refreshToken = refreshToken;
		this.authenticated = authenticated;
	}

	// OAuth 2.0 스펙 준수 생성자 (tokenType, expiresIn 포함)
	public LoginResponseDTO(String username, String role, String token, String refreshToken,
							String tokenType, Long expiresIn, boolean authenticated) {
		this.username = username;
		this.role = role;
		this.token = token;
		this.refreshToken = refreshToken;
		this.tokenType = tokenType;
		this.expiresIn = expiresIn;
		this.authenticated = authenticated;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getTokenType() {
		return tokenType;
	}

	public void setTokenType(String tokenType) {
		this.tokenType = tokenType;
	}

	public Long getExpiresIn() {
		return expiresIn;
	}

	public void setExpiresIn(Long expiresIn) {
		this.expiresIn = expiresIn;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	public boolean isAuthenticated() {
		return authenticated;
	}

	public void setAuthenticated(boolean authenticated) {
		this.authenticated = authenticated;
	}
}
