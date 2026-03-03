package com.matchhub.catconnect.domain.user.model.dto;

import com.matchhub.catconnect.global.validation.ValidEmailDomain;
import com.matchhub.catconnect.global.validation.ValidUsername;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 사용자 정보 수정 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class UserUpdateRequestDTO {

    @ValidUsername
    private String username;

    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @ValidEmailDomain
    private String email;

    @Pattern(regexp = "^01[0-9]{8,9}$", message = "올바른 휴대폰 번호 형식이 아닙니다")
    private String phoneNumber;

    @Size(min = 8, max = 20, message = "비밀번호는 8~20자여야 합니다.")
    private String password;
}
