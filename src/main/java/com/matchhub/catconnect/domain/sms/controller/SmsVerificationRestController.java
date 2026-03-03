package com.matchhub.catconnect.domain.sms.controller;

import com.matchhub.catconnect.domain.sms.model.dto.SendSmsCodeRequestDTO;
import com.matchhub.catconnect.domain.sms.model.dto.SmsResponseDTO;
import com.matchhub.catconnect.domain.sms.model.dto.VerifySmsCodeRequestDTO;
import com.matchhub.catconnect.domain.sms.service.SmsVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * SMS 인증 REST API 컨트롤러
 */
@RestController
@RequestMapping("/api/sms")
@Tag(name = "SMS 인증 API", description = "SMS 본인인증 관련 API")
public class SmsVerificationRestController {

    private final SmsVerificationService smsVerificationService;

    public SmsVerificationRestController(SmsVerificationService smsVerificationService) {
        this.smsVerificationService = smsVerificationService;
    }

    /**
     * 회원가입용 SMS 인증번호 발송
     */
    @PostMapping("/signup/send-code")
    @Operation(summary = "회원가입 SMS 인증번호 발송", description = "회원가입 시 휴대폰 본인인증을 위한 인증번호를 발송합니다.")
    public ResponseEntity<SmsResponseDTO> sendSignupCode(
            @Valid @RequestBody SendSmsCodeRequestDTO request) {
        SmsResponseDTO response = smsVerificationService.sendSignupVerificationCode(request.getPhoneNumber());
        return ResponseEntity.ok(response);
    }

    /**
     * 회원가입용 SMS 인증번호 확인
     */
    @PostMapping("/signup/verify-code")
    @Operation(summary = "회원가입 SMS 인증번호 확인", description = "발송된 SMS 인증번호를 확인합니다.")
    public ResponseEntity<SmsResponseDTO> verifySignupCode(
            @Valid @RequestBody VerifySmsCodeRequestDTO request) {
        SmsResponseDTO response = smsVerificationService.verifySignupCode(
                request.getPhoneNumber(), request.getCode());
        return ResponseEntity.ok(response);
    }
}
