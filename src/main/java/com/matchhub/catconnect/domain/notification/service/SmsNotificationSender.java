package com.matchhub.catconnect.domain.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matchhub.catconnect.domain.notification.model.enums.NotificationChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * SMS 알림 발송 구현체 (SOLAPI)
 */
@Component
public class SmsNotificationSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(SmsNotificationSender.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.solapi.api-key}")
    private String apiKey;

    @Value("${app.solapi.api-secret}")
    private String apiSecret;

    @Value("${app.solapi.sender-phone}")
    private String senderPhone;

    @Value("${app.solapi.api-url}")
    private String apiUrl;

    public SmsNotificationSender() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.SMS;
    }

    @Override
    public boolean supportsTemplate() {
        return false;
    }

    @Override
    public void send(String recipient, String message) {
        log.debug("SMS 발송: to={}", recipient);

        try {
            HttpHeaders headers = createAuthHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> messageMap = new HashMap<>();
            messageMap.put("to", recipient);
            messageMap.put("from", senderPhone);
            messageMap.put("text", message);
            messageMap.put("type", "SMS");

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("messages", Collections.singletonList(messageMap));

            String jsonBody = objectMapper.writeValueAsString(requestBody);
            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.debug("SMS 발송 성공: to={}", recipient);
            } else {
                log.error("SMS 발송 실패: to={}, status={}, body={}",
                        recipient, response.getStatusCode(), response.getBody());
                throw new RuntimeException("SMS 발송에 실패했습니다.");
            }
        } catch (Exception e) {
            log.error("SMS 발송 중 오류 발생: to={}, error={}", recipient, e.getMessage());
            throw new RuntimeException("SMS 발송에 실패했습니다.", e);
        }
    }

    @Override
    public void sendWithTemplate(String recipient, String templateName, Map<String, Object> variables) {
        // SMS는 템플릿 미지원, 단순 메시지로 대체
        String message = (String) variables.getOrDefault("message", "알림 메시지입니다.");
        send(recipient, message);
    }

    /**
     * SOLAPI 인증 헤더 생성 (HMAC-SHA256)
     */
    private HttpHeaders createAuthHeaders() {
        String date = ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT);
        String salt = UUID.randomUUID().toString().replace("-", "");
        String signature = generateSignature(date, salt);

        String authorization = String.format(
                "HMAC-SHA256 apiKey=%s, date=%s, salt=%s, signature=%s",
                apiKey, date, salt, signature);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authorization);
        return headers;
    }

    /**
     * HMAC-SHA256 서명 생성
     */
    private String generateSignature(String date, String salt) {
        try {
            String data = date + salt;
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    apiSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("서명 생성 실패", e);
        }
    }

    /**
     * 바이트 배열을 16진수 문자열로 변환
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
