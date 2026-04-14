package com.matchhub.catconnect.domain.auth.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 토큰 재발급 요청 DTO
 *
 * Refresh Token을 쿠키로 전달하는 경우 이 DTO는 사용하지 않아도 됨
 * Authorization 헤더나 요청 바디로 전달하는 경우를 위해 정의
 */
@Schema(description = "토큰 재발급 요청")
public class TokenRefreshRequestDTO {

    @Schema(description = "Refresh Token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String refreshToken;

    public TokenRefreshRequestDTO() {
    }

    public TokenRefreshRequestDTO(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
