package com.matchhub.catconnect.domain.auth.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 로그인 요청 DTO
 */
@Schema(description = "로그인 요청 정보")
public class LoginRequestDTO {

    @Schema(description = "사용자 이름", example = "testuser", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @Schema(description = "비밀번호", example = "password123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    public LoginRequestDTO() {
    }

    public LoginRequestDTO(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
