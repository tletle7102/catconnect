package com.matchhub.catconnect.domain.sms.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * SMS 인증번호 발송 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class SendSmsCodeRequestDTO {

    @NotBlank(message = "휴대폰 번호는 필수입니다")
    @Pattern(regexp = "^01[0-9]{8,9}$", message = "올바른 휴대폰 번호 형식이 아닙니다")
    private String phoneNumber;

    public SendSmsCodeRequestDTO(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
