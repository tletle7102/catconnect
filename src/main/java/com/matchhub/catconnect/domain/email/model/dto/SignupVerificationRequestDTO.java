package com.matchhub.catconnect.domain.email.model.dto;

import com.matchhub.catconnect.global.validation.ValidEmailDomain;
import com.matchhub.catconnect.global.validation.ValidUsername;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 회원가입 인증 요청 DTO
 */
@Getter
@Setter
public class SignupVerificationRequestDTO {

    @NotBlank(message = "사용자 이름은 필수입니다.")
    @ValidUsername
    private String username;

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @ValidEmailDomain
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8, max = 20, message = "비밀번호는 8~20자여야 합니다.")
    private String password;

    public SignupVerificationRequestDTO() {
    }
}
