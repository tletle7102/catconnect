package com.matchhub.catconnect.domain.user.model.dto;

import com.matchhub.catconnect.global.validation.ValidEmailDomain;
import com.matchhub.catconnect.global.validation.ValidUsername;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequestDTO {
    @NotBlank(message = "사용자 이름은 필수입니다.")
    @ValidUsername
    private String username;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 6, max = 100, message = "비밀번호는 6~100자여야 합니다.")
    private String password;

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "유효한 이메일 형식이어야 합니다.")
    @ValidEmailDomain
    private String email;

}