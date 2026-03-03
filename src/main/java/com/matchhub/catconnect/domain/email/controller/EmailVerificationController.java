package com.matchhub.catconnect.domain.email.controller;

import com.matchhub.catconnect.domain.email.service.EmailVerificationService;
import com.matchhub.catconnect.global.exception.AppException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 이메일 인증 뷰 컨트롤러
 */
@Controller
public class EmailVerificationController {

    private static final Logger log = LoggerFactory.getLogger(EmailVerificationController.class);

    private final EmailVerificationService emailVerificationService;

    public EmailVerificationController(EmailVerificationService emailVerificationService) {
        this.emailVerificationService = emailVerificationService;
    }

    /**
     * 이메일 인증 링크 클릭 시 처리
     */
    @GetMapping("/verify-email")
    public String verifyEmail(@RequestParam String token, Module module, Model model) {
       try {
           emailVerificationService.verifyEmailAndCreateUser(token);
           model.addAttribute("success", true);
           model.addAttribute("message", "이메일 인증이 완료되어 회원가입이 완료되었습니다.");
       } catch (AppException e) {
           log.warn("이메일 인증 실패: {}",e.getMessage());
           model.addAttribute("success", false);
           model.addAttribute("message", e.getMessage());
       }
       return "auth/verify-email-result";
    }

    /**
     * 회원가입 페이지 (이메일 인증 방식)
     */
    @GetMapping("/signup")
    public String signupPage() {
        return "auth/signup";
    }

    /**
     * 아이디 찾기 페이지
     */
    @GetMapping("/find-username")
    public String findUsernamePage() {
        return "/auth/find-username";
    }

    /**
     * 비밀번호 찾기 페이지
     */
    @GetMapping("/find-password")
    public String findPasswordPage() {
        return "/auth/find-password";
    }
}
