package com.matchhub.catconnect.domain.sms.service;

import com.matchhub.catconnect.domain.sms.model.dto.SmsResponseDTO;
import com.matchhub.catconnect.domain.sms.model.entity.SmsVerificationToken;
import com.matchhub.catconnect.domain.sms.model.enums.SmsTokenType;
import com.matchhub.catconnect.domain.sms.repository.SmsVerificationTokenRepository;
import com.matchhub.catconnect.global.exception.AppException;
import com.matchhub.catconnect.global.exception.Domain;
import com.matchhub.catconnect.global.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Optional;

/**
 * SMS 인증 서비스
 * 인증번호 발송 및 검증 로직 담당
 */
@Service
@Transactional
public class SmsVerificationService {

    private static final Logger log = LoggerFactory.getLogger(SmsVerificationService.class);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final SmsService smsService;
    private final SmsVerificationTokenRepository tokenRepository;

    @Value("${app.sms.verification-expiry-minutes}")
    private int expiryMinutes;

    public SmsVerificationService(SmsService smsService, SmsVerificationTokenRepository tokenRepository) {
        this.smsService = smsService;
        this.tokenRepository = tokenRepository;
    }

    /**
     * 회원가입용 SMS 인증번호 발송
     */
    public SmsResponseDTO sendSignupVerificationCode(String phoneNumber) {
        return sendVerificationCode(phoneNumber, SmsTokenType.SIGNUP);
    }

    /**
     * SMS 인증번호 발송 (범용)
     */
    public SmsResponseDTO sendVerificationCode(String phoneNumber, SmsTokenType tokenType) {
        log.debug("SMS 인증번호 발송: phoneNumber={}, tokenType={}", phoneNumber, tokenType);

        // 기존 토큰 삭제
        tokenRepository.deleteByPhoneNumberAndTokenType(phoneNumber, tokenType);

        // 6자리 인증번호 생성
        String code = generateVerificationCode();

        // 토큰 저장
        SmsVerificationToken token = new SmsVerificationToken(phoneNumber, code, tokenType, expiryMinutes);
        tokenRepository.save(token);

        // SMS 발송
        boolean sent = smsService.sendVerificationCode(phoneNumber, code);
        if (!sent) {
            log.error("SMS 발송 실패: phoneNumber={}", phoneNumber);
            throw new AppException(Domain.SMS, ErrorCode.SMS_SEND_FAILED, "SMS 발송에 실패했습니다.");
        }

        log.debug("SMS 인증번호 발송 완료: phoneNumber={}", phoneNumber);
        return SmsResponseDTO.success("인증번호가 발송되었습니다.");
    }

    /**
     * 회원가입용 SMS 인증번호 확인
     */
    public SmsResponseDTO verifySignupCode(String phoneNumber, String code) {
        return verifyCode(phoneNumber, code, SmsTokenType.SIGNUP);
    }

    /**
     * SMS 인증번호 확인 (범용)
     */
    public SmsResponseDTO verifyCode(String phoneNumber, String code, SmsTokenType tokenType) {
        log.debug("SMS 인증번호 확인: phoneNumber={}, tokenType={}", phoneNumber, tokenType);

        Optional<SmsVerificationToken> tokenOpt = tokenRepository
                .findByPhoneNumberAndTokenTypeAndUsedFalse(phoneNumber, tokenType);

        if (tokenOpt.isEmpty()) {
            log.warn("인증 토큰 없음: phoneNumber={}", phoneNumber);
            throw new AppException(Domain.SMS, ErrorCode.SMS_TOKEN_NOT_FOUND, "인증번호를 먼저 요청해주세요.");
        }

        SmsVerificationToken token = tokenOpt.get();

        // 만료 확인
        if (token.isExpired()) {
            log.warn("인증 토큰 만료: phoneNumber={}", phoneNumber);
            throw new AppException(Domain.SMS, ErrorCode.SMS_TOKEN_EXPIRED, "인증번호가 만료되었습니다. 다시 요청해주세요.");
        }

        // 인증번호 일치 확인
        if (!token.getCode().equals(code)) {
            log.warn("인증번호 불일치: phoneNumber={}", phoneNumber);
            throw new AppException(Domain.SMS, ErrorCode.SMS_CODE_INVALID, "인증번호가 일치하지 않습니다.");
        }

        // 인증 완료 처리
        token.markAsVerified();
        tokenRepository.save(token);

        log.debug("SMS 인증 완료: phoneNumber={}", phoneNumber);
        return SmsResponseDTO.success("인증이 완료되었습니다.");
    }

    /**
     * 휴대폰 번호 인증 완료 여부 확인
     */
    @Transactional(readOnly = true)
    public boolean isPhoneVerified(String phoneNumber, SmsTokenType tokenType) {
        return tokenRepository.findByPhoneNumberAndTokenTypeAndVerifiedTrue(phoneNumber, tokenType)
                .isPresent();
    }

    /**
     * 인증 완료된 토큰 사용 처리 (회원가입 완료 시 호출)
     */
    public void markTokenAsUsed(String phoneNumber, SmsTokenType tokenType) {
        tokenRepository.findByPhoneNumberAndTokenTypeAndVerifiedTrue(phoneNumber, tokenType)
                .ifPresent(token -> {
                    token.markAsUsed();
                    tokenRepository.save(token);
                });
    }

    /**
     * 6자리 인증번호 생성
     */
    private String generateVerificationCode() {
        int code = 100000 + RANDOM.nextInt(900000);
        return String.valueOf(code);
    }
}
