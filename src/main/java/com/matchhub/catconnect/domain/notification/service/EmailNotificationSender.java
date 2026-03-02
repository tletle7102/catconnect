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
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * 이메일 알림 발송 구현체
 * Thymeleaf 템플릿을 사용한 HTML 이메일 발송 지원
 */
@Component
public class EmailNotificationSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationSender.class);

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.mail.from-name}")
    private String fromName;

    public EmailNotificationSender(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
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
        sendHtmlEmail(recipient, "[CatConnect] 알림", wrqpSimpleMessage(message));
    }

    @Override
    public void sendWithTemplate(String recipient, String templateName, Map<String, Object> variables) {
        log.debug("템플릿 이메일 발송: to={}, template={}", recipient, templateName);

        Context context = new Context();
        variables.forEach(context ::setVariable);

        String subject = extractSubject(templateName, variables);
        String templatePath = "email/" + templateName;
        String content = templateEngine.process(templatePath, context);

        sendHtmlEmail(recipient, subject, content);
    }

    /**
     * 템플릿 이름에 따른 제목 추출
     */
    private String extractSubject(String templateName, Map<String, Object> variables) {
        return switch (templateName) {
            case "signup-verification" -> "[CatConnect] 회원가입 이메일 인증";
            case "signup-code" -> "[CatConnect] 회원가입 이메일 인증번호";
            case "password-reset" -> "[CatConnect] 비밀번호 재설정 인증번호";
            default -> (String) variables.getOrDefault("subject", "[CatConnect] 알림");
        };
    }

    /**
     * 단순 메시지를 HTML로 래핑
     */
    private String wrqpSimpleMessage(String message) {
        return """
				<!DOCTYPE html>
				<html>
				<head><meta charset="UTF-8"></head>
				<body style="font-family: sans-serif; padding: 20px;">
					<p>%s</p>
				</body>
				</html>
				""".formatted(message.replace("\n", "<br>"));
    }

    /**
     * HTML 이메일 발송
     */
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
