package com.matchhub.catconnect.domain.email.model.dto;

import com.matchhub.catconnect.global.validation.ValidEmailDomain;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 인증번호 확인 요청 DTO
 */
@Getter
@Setter
public class VerifyCodeRequestDTO {

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @ValidEmailDomain
    private String email;

    @NotBlank(message = "인증번호는 필수입니다.")
    @Size(min = 6, max = 6, message = "인증번호는 6자리입니다.")
    private String code;

    public VerifyCodeRequestDTO() {
    }
}
