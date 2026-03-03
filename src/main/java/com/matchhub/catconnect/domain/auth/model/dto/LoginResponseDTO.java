package com.matchhub.catconnect.domain.auth.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 로그인 응답 DTO
 */
@Schema(description = "로그인 응답 정보")
public class LoginResponseDTO {

    @Schema(description = "사용자 이름", example = "testuser")
    private String username;

    @Schema(description = "사용자 권한", example = "USER")
    private String role;

    @Schema(description = "JWT 토큰")
    private String token;

    @Schema(description = "인증 여부", example = "true")
    private boolean authenticated;

    public LoginResponseDTO() {
    }

    public LoginResponseDTO(String username, String role, String token, boolean authenticated) {
        this.username = username;
        this.role = role;
        this.token = token;
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

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }
}
