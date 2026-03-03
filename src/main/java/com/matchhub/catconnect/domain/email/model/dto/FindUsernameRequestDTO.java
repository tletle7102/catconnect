package com.matchhub.catconnect.domain.email.model.dto;

import com.matchhub.catconnect.global.validation.ValidEmailDomain;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;

/**
 * 아이디 찾기 요청 DTO
 */
@Getter
@Setter
public class FindUsernameRequestDTO {

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @ValidEmailDomain
    private String email;

    public FindUsernameRequestDTO() {
    }
}
