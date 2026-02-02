package com.matchhub.catconnect.domain.email.model.enums;

/**
 * 이메일 인증 토큰 타입
 */
public enum TokenType {
    EMAIL_VERIFICATION,  // 회원가입 이메일 인증 (링크 방식)
    SIGNUP_CODE,         // 회원가입 이메일 인증 (인증번호 방식)
    PASSWORD_RESET       // 비밀번호 재설정
}
