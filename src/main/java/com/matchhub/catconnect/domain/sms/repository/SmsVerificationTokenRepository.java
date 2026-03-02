package com.matchhub.catconnect.domain.sms.repository;

import com.matchhub.catconnect.domain.sms.model.entity.SmsVerificationToken;
import com.matchhub.catconnect.domain.sms.model.enums.SmsTokenType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * SMS 인증 토큰 레포지토리
 */
@Repository
public interface SmsVerificationTokenRepository extends JpaRepository<SmsVerificationToken, Long> {

    /**
     * 휴대폰 번호와 토큰 타입으로 가장 최근 토큰 조회
     */
    Optional<SmsVerificationToken> findTopByPhoneNumberAndTokenTypeOrderByCreatedDttmDesc(
            String phoneNumber, SmsTokenType tokenType);

    /**
     * 휴대폰 번호와 토큰 타입으로 미사용 토큰 조회
     */
    Optional<SmsVerificationToken> findByPhoneNumberAndTokenTypeAndUsedFalse(
            String phoneNumber, SmsTokenType tokenType);

    /**
     * 휴대폰 번호와 토큰 타입으로 인증 완료된 토큰 조회
     */
    Optional<SmsVerificationToken> findByPhoneNumberAndTokenTypeAndVerifiedTrue(
            String phoneNumber, SmsTokenType tokenType);

    /**
     * 휴대폰 번호로 기존 토큰 삭제 (새 토큰 발급 전)
     */
    void deleteByPhoneNumberAndTokenType(String phoneNumber, SmsTokenType tokenType);
}
