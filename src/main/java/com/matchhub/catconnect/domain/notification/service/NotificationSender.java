package com.matchhub.catconnect.domain.notification.service;

import com.matchhub.catconnect.domain.notification.model.enums.NotificationChannel;

import java.util.Map;

/**
 * 알림 발송 인터페이스
 * 각 채널별 구현체가 이 인터페이스를 구현함
 */
public interface NotificationSender {

    /**
     * 이 발송자가 지원하는 채널 반환
     */
    NotificationChannel getChannel();

    /**
     * 단순 메시지 발송
     * @param recipient 수신자 (이메일 주소 또는 전화번호)
     * @param message 메시지 내용
     */
    void send(String recipient, String message);

    /**
     * 템플릿 기반 메시지 발송
     * @param recipient 수신자 (이메일 주소 또는 전화번호)
     * @param templateName 템플릿 이름
     * @param variables 템플릿 변수
     */
    void sendWithTemplate(String recipient, String templateName, Map<String, Object> variables);

    /**
     * 템플릿 기반 발송 지원 여부
     */
    default boolean supportsTemplate() {
        return false;
    }
}
