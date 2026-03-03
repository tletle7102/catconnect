package com.matchhub.catconnect.domain.sms.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * SMS API 응답 DTO
 */
@Getter
@AllArgsConstructor
public class SmsResponseDTO {

    private boolean success;
    private String message;

    public static SmsResponseDTO success(String message) {
        return new SmsResponseDTO(true, message);
    }

    public static SmsResponseDTO failure(String message) {
        return new SmsResponseDTO(false, message);
    }
}
