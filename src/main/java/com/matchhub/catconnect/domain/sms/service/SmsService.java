package com.matchhub.catconnect.domain.sms.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * SMS 발송 서비스 (SOLAPI 연동)
 * 범용 SMS 발송 기능 제공, 추후 다양한 용도로 확장 가능
 */
@Service
public class SmsService {

    private static final Logger log = LoggerFactory.getLogger(SmsService.class);

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

    public SmsService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * SMS 발송
     * @param to 수신자 전화번호
     * @param text 메시지 내용 (90바이트 이하)
     * @return 발송 성공 여부
     */
    public boolean sendSms(String to, String text) {
        log.debug("SMS 발송 시작: to={}", to);

        try {
            HttpHeaders headers = createAuthHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> message = new HashMap<>();
            message.put("to", to);
            message.put("from", senderPhone);
            message.put("text", text);
            message.put("type", "SMS");

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("messages", Collections.singletonList(message));

            String jsonBody = objectMapper.writeValueAsString(requestBody);

            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.debug("SMS 발송 성공: to={}", to);
                return true;
            } else {
                log.error("SMS 발송 실패: to={}, status={}, body={}",
                        to, response.getStatusCode(), response.getBody());
                return false;
            }
        } catch (Exception e) {
            log.error("SMS 발송 중 오류 발생: to={}, error={}", to, e.getMessage());
            return false;
        }
    }

    /**
     * 인증번호 SMS 발송
     * @param to 수신자 전화번호
     * @param code 인증번호
     * @return 발송 성공 여부
     */
    public boolean sendVerificationCode(String to, String code) {
        String text = "[CatConnect] 인증번호: " + code + " (3분간 유효)";
        return sendSms(to, text);
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
