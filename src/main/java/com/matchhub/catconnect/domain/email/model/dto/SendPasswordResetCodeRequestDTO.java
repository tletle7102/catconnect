package com.matchhub.catconnect.domain.email.model.dto;

import com.matchhub.catconnect.global.validation.ValidEmailDomain;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

/**
 * 비밀번호 재설정 인증번호 발송 요청 DTO
 */
@Getter
@Setter
public class SendPasswordResetCodeRequestDTO {

    @NotBlank(message = "사용자 이름은 필수입니다.")
    private String username;

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @ValidEmailDomain
    private String email;

    public SendPasswordResetCodeRequestDTO() {
    }
}
