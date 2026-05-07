package com.matchhub.catconnect.domain.notification.service;

import com.matchhub.catconnect.domain.notification.model.enums.NotificationChannel;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * 이메일 알림 발송 구현체
 * 순수 HTML 문자열 기반 이메일 발송
 */
@Component
public class EmailNotificationSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationSender.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.mail.from-name}")
    private String fromName;

    public EmailNotificationSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public boolean supportsTemplate() {
        return true;
    }

    @Override
    public void send(String recipient, String message) {
        log.debug("이메일 발송: to={}", recipient);
        sendHtmlEmail(recipient, "[CatConnect] 알림", wrapSimpleMessage(message));
    }

    @Override
    public void sendWithTemplate(String recipient, String templateName, Map<String, Object> variables) {
        log.debug("템플릿 이메일 발송: to={}, template={}", recipient, templateName);

        String subject = extractSubject(templateName, variables);
        String content = buildTemplateContent(templateName, variables);

        sendHtmlEmail(recipient, subject, content);
    }

    private String buildTemplateContent(String templateName, Map<String, Object> variables) {
        return switch (templateName) {
            case "signup-verification" -> buildSignupVerificationEmail(variables);
            case "signup-code" -> buildSignupCodeEmail(variables);
            case "password-reset" -> buildPasswordResetEmail(variables);
            default -> wrapSimpleMessage(String.valueOf(variables.getOrDefault("message", "")));
        };
    }

    private String buildSignupVerificationEmail(Map<String, Object> variables) {
        String verificationUrl = String.valueOf(variables.getOrDefault("verificationUrl", ""));
        return wrapHtml("""
            <h2 style="color: #10ba8c;">CatConnect 회원가입 인증</h2>
            <p>아래 링크를 클릭하여 이메일 인증을 완료해주세요.</p>
            <p><a href="%s" style="display: inline-block; padding: 12px 24px; background-color: #10ba8c; color: white; text-decoration: none; border-radius: 8px;">이메일 인증하기</a></p>
            <p style="color: #999; font-size: 12px;">본인이 요청하지 않은 경우 이 메일을 무시해주세요.</p>
            """.formatted(verificationUrl));
    }

    private String buildSignupCodeEmail(Map<String, Object> variables) {
        String code = String.valueOf(variables.getOrDefault("code", ""));
        int expiryMinutes = (int) variables.getOrDefault("expiryMinutes", 10);
        return wrapHtml("""
            <h2 style="color: #10ba8c;">CatConnect 이메일 인증번호</h2>
            <p>아래 인증번호를 입력해주세요.</p>
            <p style="font-size: 32px; font-weight: bold; letter-spacing: 8px; color: #10ba8c;">%s</p>
            <p style="color: #999;">인증번호는 %d분간 유효합니다.</p>
            """.formatted(code, expiryMinutes));
    }

    private String buildPasswordResetEmail(Map<String, Object> variables) {
        String code = String.valueOf(variables.getOrDefault("code", ""));
        int expiryMinutes = (int) variables.getOrDefault("expiryMinutes", 5);
        return wrapHtml("""
            <h2 style="color: #10ba8c;">CatConnect 비밀번호 재설정</h2>
            <p>아래 인증번호를 입력해주세요.</p>
            <p style="font-size: 32px; font-weight: bold; letter-spacing: 8px; color: #10ba8c;">%s</p>
            <p style="color: #999;">인증번호는 %d분간 유효합니다.</p>
            """.formatted(code, expiryMinutes));
    }

    private String wrapHtml(String body) {
        return """
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"></head>
            <body style="font-family: 'Pretendard', sans-serif; padding: 20px; max-width: 600px; margin: 0 auto;">
                %s
            </body>
            </html>
            """.formatted(body);
    }

    private String extractSubject(String templateName, Map<String, Object> variables) {
        return switch (templateName) {
            case "signup-verification" -> "[CatConnect] 회원가입 이메일 인증";
            case "signup-code" -> "[CatConnect] 회원가입 이메일 인증번호";
            case "password-reset" -> "[CatConnect] 비밀번호 재설정 인증번호";
            default -> (String) variables.getOrDefault("subject", "[CatConnect] 알림");
        };
    }

    private String wrapSimpleMessage(String message) {
        return wrapHtml("<p>%s</p>".formatted(message.replace("\n", "<br>")));
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.debug("이메일 발송 성공: to={}, subject={}", to, subject);
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("이메일 발송 실패: to={}, error={}", to, e.getMessage());
            throw new RuntimeException("이메일 발송에 실패했습니다.", e);
        }
    }
}
