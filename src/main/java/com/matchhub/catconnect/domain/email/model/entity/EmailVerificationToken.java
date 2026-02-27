package com.matchhub.catconnect.domain.email.model.entity;

import com.matchhub.catconnect.common.model.entity.BaseEntity;
import com.matchhub.catconnect.domain.email.model.enums.TokenType;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 이메일 인증 토큰 엔티티
 * 회원가입 이메일 인증 및 비밀번호 재설정에 사용
 */
@Entity
@Table(name = "tb_email_verification_token")
@Getter
public class EmailVerificationToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TokenType tokenType;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    @Column(nullable = false)
    private boolean used = false;

    // 회원가입 시 임시 저장할 사용자 정보
    private String username;
    private String password; // 암호화된 비밀번호

    public EmailVerificationToken() {}

    /**
     * 회원가입 이메일 인증 토큰 생성
     */
    public EmailVerificationToken(String email, String token, String username, String password, int expiryMinutes) {
        this.email = email;
        this.token = token;
        this.username = username;
        this.password = password;
        this.tokenType = TokenType.EMAIL_VERIFICATION;
        this.expiryDate = LocalDateTime.now().plusMinutes(expiryMinutes);
    }

    /**
     * 비밀번호 재설정 인증번호 토큰 생성
     */
    public EmailVerificationToken(String email, String token, int expiryMinutes) {
        this.email = email;
        this.token = token;
        this.tokenType = TokenType.PASSWORD_RESET;
        this.expiryDate = LocalDateTime.now().plusMinutes(expiryMinutes);
    }

    /**
     * 회원가입 이메일 인증번호 토큰 생성 (인증번호 방식)
     */
    public EmailVerificationToken(String email, String token, int expiryMinutes, TokenType tokenType) {
        this.email = email;
        this.token = token;
        this.tokenType = tokenType;
        this.expiryDate = LocalDateTime.now().plusMinutes(expiryMinutes);
    }

    /**
     * 토큰 만료 여부 확인
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }

    /**
     * 토큰 사용 처리
     */
    public void markAsUsed() {
        this.used = true;
    }
}
