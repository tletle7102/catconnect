package com.matchhub.catconnect.domain.sms.model.entity;

import com.matchhub.catconnect.common.model.entity.BaseEntity;
import com.matchhub.catconnect.domain.sms.model.enums.SmsTokenType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * SMS 인증 토큰 엔티티
 * 회원가입 휴대폰 본인인증에 사용
 */
@Entity
@Table(name = "tb_sms_verification_token")
@Getter
@NoArgsConstructor
public class SmsVerificationToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SmsTokenType tokenType;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    @Column(nullable = false)
    private boolean used = false;

    @Column(nullable = false)
    private boolean verified = false;

    /**
     * SMS 인증 토큰 생성
     */
    public SmsVerificationToken(String phoneNumber, String code, SmsTokenType tokenType, int expiryMinutes) {
        this.phoneNumber = phoneNumber;
        this.code = code;
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

    /**
     * 인증 완료 처리
     */
    public void markAsVerified() {
        this.verified = true;
    }
}
