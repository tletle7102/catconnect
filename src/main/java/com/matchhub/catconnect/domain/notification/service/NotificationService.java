package com.matchhub.catconnect.domain.notification.service;

import com.matchhub.catconnect.domain.notification.model.enums.NotificationChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 통합 알림 서비스
 * 다양한 채널(이메일, SMS 등)을 통해 알림을 발송하는 통합 인터페이스 제공
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final Map<NotificationChannel, NotificationSender> senders;

    public NotificationService(List<NotificationSender> senderList) {
        this.senders = senderList.stream()
                .collect(Collectors.toMap(NotificationSender::getChannel, Function.identity()));
        log.debug("알림 서비스 초기화 완료: 등록된 채널={}", senders.keySet());
    }

    /**
     * 단순 메시지 발송
     * @param recipient 수신자 (이메일 주소 또는 전화번호)
     * @param channel 발송 채널
     * @param message 메시지 내용
     */
    public void send(String recipient, NotificationChannel channel, String message) {
        log.debug("알림 발송: recipient={}, channel={}", recipient, channel);

        NotificationSender sender = getSender(channel);
        sender.send(recipient, message);
    }

    /**
     * 템플릿 기반 메시지 발송
     * @param recipient 수신자
     * @param channel 발송 채널
     * @param templateName 템플릿 이름
     * @param variables 템플릿 변수
     */
    public void sendWithTemplate(String recipient, NotificationChannel channel,
                                 String templateName, Map<String, Object> variables) {
        log.debug("템플릿 알림 발송: recipient={}, channel={}, template={}",
                recipient, channel, templateName);

        NotificationSender sender = getSender(channel);

        if (!sender.supportsTemplate()) {
            log.warn("채널 {}는 템플릿을 지원하지 않습니다. 단순 메시지로 대체합니다.", channel);
        }

        sender.sendWithTemplate(recipient, templateName, variables);
    }

    /**
     * 여러 채널로 동시 발송
     * @param recipient 수신자
     * @param channels 발송 채널 목록
     * @param message 메시지 내용
     */
    public void sendToMultipleChannels(String recipient, List<NotificationChannel> channels, String message) {
        log.debug("다중 채널 알림 발송: recipient={}, channels={}", recipient, channels);

        for (NotificationChannel channel : channels) {
            try {
                send(recipient, channel, message);
            } catch (Exception e) {
                log.error("채널 {} 발송 실패: {}", channel, e.getMessage());
            }
        }
    }

    /**
     * 특정 채널 지원 여부 확인
     */
    public boolean isChannelSupported(NotificationChannel channel) {
        return senders.containsKey(channel);
    }

    /**
     * 채널별 발송자 조회
     */
    private NotificationSender getSender(NotificationChannel channel) {
        NotificationSender sender = senders.get(channel);
        if (sender == null) {
            throw new IllegalArgumentException("지원하지 않는 알림 채널입니다: " + channel);
        }
        return sender;
    }
}
