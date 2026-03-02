package com.matchhub.catconnect.domain.notification.model.enums;

/**
 * 알림 발송 채널
 */
public enum NotificationChannel {
    EMAIL("이메일"),
    SMS("문자메시지"),
    KAKAO("카카오 알림톡");

    private final String description;

    NotificationChannel(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
