package com.matchhub.catconnect.domain.auth.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 토큰 재발급 응답 DTO
 *
 * OAuth 2.0 스펙(RFC 6749)에 따라 token_type, expires_in 포함
 */
@Schema(description = "토큰 재발급 응답")
public class TokenRefreshResponseDTO {

    @Schema(description = "새로운 Access Token")
    private String accessToken;

    @Schema(description = "토큰 타입 (OAuth 2.0 스펙)", example = "Bearer")
    private String tokenType = "Bearer";

    @Schema(description = "Access Token 만료 시간 (초)", example = "900")
    private Long expiresIn;

    @Schema(description = "새로운 Refresh Token (Rotation 적용 시)")
    private String refreshToken;

    @Schema(description = "사용자 이름")
    private String username;

    @Schema(description = "사용자 권한")
    private String role;

    public TokenRefreshResponseDTO() {
    }

    // 기존 생성자 (하위 호환성)
    public TokenRefreshResponseDTO(String accessToken, String refreshToken, String username, String role) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.username = username;
        this.role = role;
    }

    // OAuth 2.0 스펙 준수 생성자
    public TokenRefreshResponseDTO(String accessToken, String refreshToken, String tokenType,
                                   Long expiresIn, String username, String role) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
        this.username = username;
        this.role = role;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
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
}
