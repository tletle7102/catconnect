package com.matchhub.catconnect.domain.email.controller;

import com.matchhub.catconnect.domain.email.model.dto.*;
import com.matchhub.catconnect.domain.email.service.EmailVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 이메일 인증 REST API 컨트롤러
 */
@Tag(name = "이메일 인증 API", description = "회원가입 이메일 인증 및 비밀번호 재설정 API")
@RestController
@RequestMapping("/api/auth")
public class EmailVerificationRestController {

    private final EmailVerificationService emailVerificationService;

    public EmailVerificationRestController(EmailVerificationService emailVerificationService) {
        this.emailVerificationService = emailVerificationService;
    }

    /**
     * 회원가입 이메일 인증번호 발송
     */
    @Operation(summary = "회원가입 이메일 인증번호 발송", description = "입력된 이메일로 6자리 인증번호를 발송합니다.")
    @PostMapping("/signup/send-code")
    public ResponseEntity<Map<String, String>> sendSignupEmailCode(
            @Valid @RequestBody FindUsernameRequestDTO request) {

        emailVerificationService.sendSignupEmailCode(request.getEmail());

        return ResponseEntity.ok(Map.of("message", "인증번호가 이메일로 발송되었습니다."));
    }

    /**
     * 회원가입 이메일 인증번호 확인
     */
    @Operation(summary = "회원가입 이메일 인증번호 확인", description = "이메일로 발송된 인증번호를 확인합니다.")
    @PostMapping("/signup/verify-code")
    public ResponseEntity<Map<String, String>> verifySignupEmailCode(
            @Valid @RequestBody VerifyCodeRequestDTO request) {

        emailVerificationService.verifySignupEmailCode(request.getEmail(), request.getCode());

        return ResponseEntity.ok(Map.of("message", "이메일 인증이 완료되었습니다."));
    }

    /**
     * 회원가입 완료 (이메일 인증 후)
     */
    @Operation(summary = "회원가입 완료", description = "이메일 및 휴대폰 인증 완료 후 회원가입을 진행합니다.")
    @PostMapping("/signup/complete")
    public ResponseEntity<Map<String, String>> completeSignup(
            @Valid @RequestBody SignupVerificationRequestDTO request) {

        emailVerificationService.createUserAfterVerification(
                request.getUsername(),
                request.getEmail(),
                request.getPhoneNumber(),
                request.getPassword()
        );

        return ResponseEntity.ok(Map.of("message", "회원가입이 완료되었습니다."));
    }

    /**
     * 비밀번호 재설정 인증번호 발송
     */
    @Operation(summary = "비밀번호 재설정 인증번호 발송", description = "입력된 사용자 정보로 인증번호를 이메일로 발송합니다.")
    @PostMapping("/password/send-code")
    public ResponseEntity<Map<String, String>> sendPasswordResetCode(
            @Valid @RequestBody SendPasswordResetCodeRequestDTO request) {

        emailVerificationService.sendPasswordResetCode(request.getUsername(), request.getEmail());

        return ResponseEntity.ok(Map.of("message", "인증번호가 이메일로 발송되었습니다."));
    }

    /**
     * 비밀번호 재설정 인증번호 확인
     */
    @Operation(summary = "인증번호 확인", description = "이메일로 발송된 인증번호를 확인합니다.")
    @PostMapping("/password/verify-code")
    public ResponseEntity<Map<String, String>> verifyPasswordResetCode(
            @Valid @RequestBody VerifyCodeRequestDTO request) {

        emailVerificationService.verifyPasswordResetCode(request.getEmail(), request.getCode());

        return ResponseEntity.ok(Map.of("message", "인증이 완료되었습니다."));
    }

    /**
     * 비밀번호 재설정
     */
    @Operation(summary = "비밀번호 재설정", description = "인증 완료 후 새 비밀번호로 변경합니다.")
    @PostMapping("/password/reset")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Valid @RequestBody PasswordResetRequestDTO request) {

        emailVerificationService.resetPassword(
                request.getEmail(),
                request.getCode(),
                request.getNewPassword()
        );

        return ResponseEntity.ok(Map.of("message", "비밀번호가 변경되었습니다. 새 비밀번호로 로그인해주세요."));
    }

    /**
     * 아이디 찾기
     */
    @Operation(summary = "아이디 찾기", description = "이메일로 가입된 아이디를 조회합니다.")
    @PostMapping("/find-username")
    public ResponseEntity<Map<String, String>> findUsername(
            @Valid @RequestBody FindUsernameRequestDTO request) {

        String maskedUsername = emailVerificationService.findUsername(request.getEmail());

        return ResponseEntity.ok(Map.of("username", maskedUsername));
    }
}
