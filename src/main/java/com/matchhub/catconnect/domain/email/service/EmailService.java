package com.matchhub.catconnect.domain.email.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * 이메일 발송 서비스 (MIME 방식)
 * Thymeleaf 템플릿 엔진을 사용하여 HTML 이메일 렌더링
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.mail.from-name}")
    private String fromName;

    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    /**
     * 회원가입 이메일 인증 링크 발송
     */
    public void sendVerificationEmail(String to, String username, String verificationLink) {
        log.debug("회원가입 인증 이메일 발송: to={}, username={}", to, username);

        String subject = "[CatConnect] 회원가입 이메일 인증";

        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("verificationLink", verificationLink);

        String content = templateEngine.process("email/signup-verification", context);
        sendHtmlEmail(to, subject, content);
    }

    /**
     * 비밀번호 재설정 인증번호 발송
     */
    public void sendPasswordResetCode(String to, String username, String code) {
        log.debug("비밀번호 재설정 인증번호 발송: to={}, username={}", to, username);

        String subject = "[CatConnect] 비밀번호 재설정 인증번호";

        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("code", code);

        String content = templateEngine.process("email/password-reset", context);
        sendHtmlEmail(to, subject, content);
    }

    /**
     * 회원가입 이메일 인증번호 발송
     */
    public void sendSignupVerificationCode(String to, String code) {
        log.debug("회원가입 이메일 인증번호 발송: to={}", to);

        String subject = "[CatConnect] 회원가입 이메일 인증번호";

        Context context = new Context();
        context.setVariable("code", code);

        String content = templateEngine.process("email/signup-code", context);
        sendHtmlEmail(to, subject, content);
    }

    /**
     * HTML 이메일 발송 (MIME)
     */
    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true = HTML

            mailSender.send(message);
            log.debug("이메일 발송 성공: to={}, subject={}", to, subject);
        } catch (MessagingException e) {
            log.error("이메일 발송 실패: to={}, error={}", to, e.getMessage());
            throw new RuntimeException("이메일 발송에 실패했습니다.", e);
        } catch (java.io.UnsupportedEncodingException e) {
            log.error("이메일 인코딩 오류: to={}, error={}", to, e.getMessage());
            throw new RuntimeException("이메일 발송에 실패했습니다.", e);
        }
    }
}
