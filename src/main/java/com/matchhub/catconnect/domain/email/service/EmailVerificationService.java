package com.matchhub.catconnect.domain.email.service;

import com.matchhub.catconnect.domain.email.model.entity.EmailVerificationToken;
import com.matchhub.catconnect.domain.email.model.enums.TokenType;
import com.matchhub.catconnect.domain.email.repository.EmailVerificationTokenRepository;
import com.matchhub.catconnect.domain.notification.model.enums.NotificationChannel;
import com.matchhub.catconnect.domain.notification.service.NotificationService;
import com.matchhub.catconnect.domain.sms.model.enums.SmsTokenType;
import com.matchhub.catconnect.domain.sms.service.SmsVerificationService;
import com.matchhub.catconnect.domain.user.model.entity.User;
import com.matchhub.catconnect.domain.user.model.enums.Role;
import com.matchhub.catconnect.domain.user.repository.UserRepository;
import com.matchhub.catconnect.global.exception.AppException;
import com.matchhub.catconnect.global.exception.Domain;
import com.matchhub.catconnect.global.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Map;
import java.util.UUID;

/**
 * 이메일 인증 서비스
 */
@Service
@Transactional
public class EmailVerificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailVerificationService.class);

    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final PasswordEncoder passwordEncoder;
    private final SmsVerificationService smsVerificationService;

    @Value("${app.mail.verification-expiry-minutes}")
    private int verificationExpiryMinutes;

    @Value("${app.mail.password-reset-expiry-minutes}")
    private int passwordResetExpiryMinutes;

    public EmailVerificationService(
            EmailVerificationTokenRepository tokenRepository,
            UserRepository userRepository,
            NotificationService notificationService,
            PasswordEncoder passwordEncoder,
            SmsVerificationService smsVerificationService) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.passwordEncoder = passwordEncoder;
        this.smsVerificationService = smsVerificationService;
    }

    /**
     * 회원가입 이메일 인증번호 발송
     * 이메일 중복 확인 후 6자리 인증번호 발송
     */
    public void sendSignupEmailCode(String email) {
        log.debug("회원가입 이메일 인증번호 요청: email={}", email);

        // 이미 가입된 이메일인지 확인
        if (userRepository.existsByEmail(email)) {
            throw new AppException(Domain.USER, ErrorCode.USER_DUPLICATE_EMAIL, "이미 사용 중인 이메일입니다.");
        }

        // 기존 인증 토큰 삭제
        tokenRepository.deleteByEmailAndTokenType(email, TokenType.SIGNUP_CODE);

        // 6자리 인증번호 생성
        String code = generateRandomCode(6);

        EmailVerificationToken verificationToken = new EmailVerificationToken(
                email, code, verificationExpiryMinutes, TokenType.SIGNUP_CODE);
        tokenRepository.save(verificationToken);

        // 인증번호 이메일 발송
        notificationService.sendWithTemplate(email, NotificationChannel.EMAIL, "signup-code", Map.of("code", code));

        log.debug("회원가입 이메일 인증번호 발송 완료: email={}", email);
    }

    /**
     * 회원가입 이메일 인증번호 확인
     */
    public void verifySignupEmailCode(String email, String code) {
        log.debug("회원가입 이메일 인증번호 확인: email={}, code={}", email, code);

        EmailVerificationToken verificationToken = tokenRepository.findByEmailAndTokenTypeAndUsedFalse(email, TokenType.SIGNUP_CODE)
                .orElseThrow(() -> new AppException(Domain.AUTH, ErrorCode.INVALID_TOKEN, "인증번호를 먼저 요청해주세요."));

        // 인증번호 확인
        if (!verificationToken.getToken().equals(code)) {
            throw new AppException(Domain.AUTH, ErrorCode.INVALID_TOKEN, "인증번호가 일치하지 않습니다.");
        }

        // 만료 확인
        if (verificationToken.isExpired()) {
            throw new AppException(Domain.AUTH, ErrorCode.TOKEN_EXPIRED, "인증번호가 만료되었습니다. 다시 요청해주세요.");
        }

        // 토큰 사용 처리
        verificationToken.markAsUsed();
        tokenRepository.save(verificationToken);

        log.debug("회원가입 이메일 인증번호 확인 완료: email={}", email);
    }

    /**
     * 회원가입 완료 (이메일 인증 완료 후)
     */
    public User createUserAfterEmailVerification(String username, String email, String password) {
        log.debug("회원가입 완료 처리: username={}, email={}", username, email);

        // 이미 가입된 이메일인지 다시 확인
        if (userRepository.existsByEmail(email)) {
            throw new AppException(Domain.USER, ErrorCode.USER_DUPLICATE_EMAIL, "이미 사용 중인 이메일입니다.");
        }

        // 이미 가입된 사용자명인지 확인
        if (userRepository.existsByUsername(username)) {
            throw new AppException(Domain.USER, ErrorCode.USER_DUPLICATE_USERNAME, "이미 사용 중인 사용자 이름입니다.");
        }

        // 사용자 생성
        String encodedPassword = passwordEncoder.encode(password);
        User user = new User(username, email, encodedPassword, Role.USER);
        userRepository.save(user);

        log.debug("회원가입 완료: username={}", user.getUsername());
        return user;
    }

    /**
     * 회원가입 완료 (이메일 및 휴대폰 인증 완료 후)
     */
    public User createUserAfterVerification(String username, String email, String phoneNumber, String password) {
        log.debug("회원가입 완료 처리: username={}, email={}, phoneNumber={}", username, email, phoneNumber);

        // 이미 가입된 이메일인지 다시 확인
        if (userRepository.existsByEmail(email)) {
            throw new AppException(Domain.USER, ErrorCode.USER_DUPLICATE_EMAIL, "이미 사용 중인 이메일입니다.");
        }

        // 이미 가입된 사용자명인지 확인
        if (userRepository.existsByUsername(username)) {
            throw new AppException(Domain.USER, ErrorCode.USER_DUPLICATE_USERNAME, "이미 사용 중인 사용자 이름입니다.");
        }

        // 휴대폰 인증 완료 여부 확인
        if (!smsVerificationService.isPhoneVerified(phoneNumber, SmsTokenType.SIGNUP)) {
            throw new AppException(Domain.SMS, ErrorCode.SMS_NOT_VERIFIED, "휴대폰 인증이 완료되지 않았습니다.");
        }

        // 사용자 생성
        String encodedPassword = passwordEncoder.encode(password);
        User user = new User(username, email, phoneNumber, encodedPassword, Role.USER);
        userRepository.save(user);

        // 휴대폰 인증 토큰 사용 처리
        smsVerificationService.markTokenAsUsed(phoneNumber, SmsTokenType.SIGNUP);

        log.debug("회원가입 완료: username={}", user.getUsername());
        return user;
    }

    /**
     * 회원가입 이메일 인증 요청 (링크 방식 - 기존)
     * 사용자 정보를 임시 저장하고 인증 이메일 발송
     */
    public void sendSignupVerification(String username, String email, String password, String baseUrl) {
        log.debug("회원가입 인증 이메일 요청: username={}, email={}", username, email);

        // 이미 가입된 이메일인지 확인
        if (userRepository.existsByEmail(email)) {
            throw new AppException(Domain.USER, ErrorCode.USER_DUPLICATE_EMAIL, "이미 사용 중인 이메일입니다.");
        }

        // 이미 가입된 사용자명인지 확인
        if (userRepository.existsByUsername(username)) {
            throw new AppException(Domain.USER, ErrorCode.USER_DUPLICATE_USERNAME, "이미 사용 중인 사용자 이름입니다.");
        }

        // 기존 인증 토큰 삭제
        tokenRepository.deleteByEmailAndTokenType(email, TokenType.EMAIL_VERIFICATION);

        // 새 토큰 생성
        String token = UUID.randomUUID().toString();
        String encodedPassword = passwordEncoder.encode(password);

        EmailVerificationToken verificationToken = new EmailVerificationToken(
                email, token, username, encodedPassword, verificationExpiryMinutes);
        tokenRepository.save(verificationToken);

        // 인증 링크 생성 및 이메일 발송
        String verificationLink = baseUrl + "/verify-email?token=" + token;
        notificationService.sendWithTemplate(email, NotificationChannel.EMAIL, "signup-verification", Map.of("username", username, "verificationLink", verificationLink));

        log.debug("회원가입 인증 이메일 발송 완료: email={}", email);
    }

    /**
     * 이메일 인증 확인 및 회원가입 완료
     */
    public User verifyEmailAndCreateUser(String token) {
        log.debug("이메일 인증 확인: token={}", token);

        EmailVerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new AppException(Domain.AUTH, ErrorCode.INVALID_TOKEN, "유효하지 않은 인증 링크입니다."));

        // 토큰 유효성 검사
        if (verificationToken.isUsed()) {
            throw new AppException(Domain.AUTH, ErrorCode.INVALID_TOKEN, "이미 사용된 인증 링크입니다.");
        }

        if (verificationToken.isExpired()) {
            throw new AppException(Domain.AUTH, ErrorCode.TOKEN_EXPIRED, "인증 링크가 만료되었습니다. 다시 회원가입을 진행해주세요.");
        }

        if (verificationToken.getTokenType() != TokenType.EMAIL_VERIFICATION) {
            throw new AppException(Domain.AUTH, ErrorCode.INVALID_TOKEN, "잘못된 인증 링크입니다.");
        }

        // 토큰 사용 처리
        verificationToken.markAsUsed();
        tokenRepository.save(verificationToken);

        // 사용자 생성
        User user = new User(
                verificationToken.getUsername(),
                verificationToken.getEmail(),
                verificationToken.getPassword(), // 이미 암호화된 비밀번호
                Role.USER
        );
        userRepository.save(user);

        log.debug("회원가입 완료: username={}", user.getUsername());
        return user;
    }

    /**
     * 비밀번호 재설정 인증번호 발송
     */
    public void sendPasswordResetCode(String username, String email) {
        log.debug("비밀번호 재설정 인증번호 요청: username={}, email={}", username, email);

        // 사용자 존재 확인
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(Domain.USER, ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다."));

        // 이메일 일치 확인
        if (!user.getEmail().equals(email)) {
            throw new AppException(Domain.USER, ErrorCode.USER_NOT_FOUND, "사용자 정보가 일치하지 않습니다.");
        }

        // 기존 인증 토큰 삭제
        tokenRepository.deleteByEmailAndTokenType(email, TokenType.PASSWORD_RESET);

        // 6자리 인증번호 생성
        String code = generateRandomCode(6);

        EmailVerificationToken resetToken = new EmailVerificationToken(
                email, code, passwordResetExpiryMinutes);
        tokenRepository.save(resetToken);

        // 인증번호 이메일 발송
        notificationService.sendWithTemplate(email, NotificationChannel.EMAIL, "password-reset", Map.of("username", username, "code", code));

        log.debug("비밀번호 재설정 인증번호 발송 완료: email={}", email);
    }

    /**
     * 비밀번호 재설정 인증번호 확인
     */
    public void verifyPasswordResetCode(String email, String code) {
        log.debug("비밀번호 재설정 인증번호 확인: email={}, code={}", email, code);

        EmailVerificationToken resetToken = tokenRepository.findByEmailAndTokenTypeAndUsedFalse(email, TokenType.PASSWORD_RESET)
                .orElseThrow(() -> new AppException(Domain.AUTH, ErrorCode.INVALID_TOKEN, "인증번호를 먼저 요청해주세요."));

        // 인증번호 확인
        if (!resetToken.getToken().equals(code)) {
            throw new AppException(Domain.AUTH, ErrorCode.INVALID_TOKEN, "인증번호가 일치하지 않습니다.");
        }

        // 만료 확인
        if (resetToken.isExpired()) {
            throw new AppException(Domain.AUTH, ErrorCode.TOKEN_EXPIRED, "인증번호가 만료되었습니다. 다시 요청해주세요.");
        }

        log.debug("비밀번호 재설정 인증번호 확인 완료: email={}", email);
    }

    /**
     * 비밀번호 재설정
     */
    public void resetPassword(String email, String code, String newPassword) {
        log.debug("비밀번호 재설정: email={}", email);

        // 인증번호 다시 확인
        EmailVerificationToken resetToken = tokenRepository.findByEmailAndTokenTypeAndUsedFalse(email, TokenType.PASSWORD_RESET)
                .orElseThrow(() -> new AppException(Domain.AUTH, ErrorCode.INVALID_TOKEN, "인증번호를 먼저 요청해주세요."));

        if (!resetToken.getToken().equals(code)) {
            throw new AppException(Domain.AUTH, ErrorCode.INVALID_TOKEN, "인증번호가 일치하지 않습니다.");
        }

        if (resetToken.isExpired()) {
            throw new AppException(Domain.AUTH, ErrorCode.TOKEN_EXPIRED, "인증번호가 만료되었습니다. 다시 요청해주세요.");
        }

        // 토큰 사용 처리
        resetToken.markAsUsed();
        tokenRepository.save(resetToken);

        // 사용자 비밀번호 변경
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(Domain.USER, ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다."));

        String encodedPassword = passwordEncoder.encode(newPassword);
        user.update(user.getUsername(), user.getEmail(), user.getPhoneNumber(), encodedPassword);
        userRepository.save(user);

        log.debug("비밀번호 재설정 완료: email={}", email);
    }

    /**
     * 아이디 찾기 (이메일로 사용자명 조회)
     */
    @Transactional(readOnly = true)
    public String findUsername(String email) {
        log.debug("아이디 찾기: email={}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(Domain.USER, ErrorCode.USER_NOT_FOUND, "해당 이메일로 가입된 계정이 없습니다."));

        // 아이디 일부 마스킹 (예: test123 -> te***23)
        String username = user.getUsername();
        if (username.length() <= 4) {
            return username.charAt(0) + "***";
        }
        return username.substring(0, 2) + "***" + username.substring(username.length() - 2);
    }

    /**
     * 랜덤 숫자 코드 생성
     */
    private String generateRandomCode(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}