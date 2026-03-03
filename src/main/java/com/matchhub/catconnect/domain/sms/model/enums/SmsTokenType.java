package com.matchhub.catconnect.domain.sms.model.enums;

/**
 * SMS 인증 토큰 용도 구분
 * 추후 새로운 인증 용도 추가 시 확장 가능
 */
public enum SmsTokenType {
    SIGNUP,           // 회원가입 본인인증
    PASSWORD_RESET,   // 비밀번호 재설정
    PHONE_CHANGE      // 휴대폰 번호 변경
}
