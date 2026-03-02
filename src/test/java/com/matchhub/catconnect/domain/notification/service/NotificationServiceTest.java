package com.matchhub.catconnect.domain.notification.service;

import com.matchhub.catconnect.domain.notification.model.enums.NotificationChannel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * NotificationService 통합 테스트
 */
@DisplayName("NotificationService 테스트")
@SpringBootTest
class NotificationServiceTest {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceTest.class);

    @Autowired
    private NotificationService notificationService;

    @Nested
    @DisplayName("채널 지원 테스트")
    class ChannelSupportTests {

        @Test
        @DisplayName("이메일 채널 지원 확인")
        void testEmailChannelSupported() {
            log.debug("이메일 채널 지원 확인 테스트 시작");

            assertTrue(notificationService.isChannelSupported(NotificationChannel.EMAIL));

            log.debug("이메일 채널 지원 확인 테스트 완료");
        }

        @Test
        @DisplayName("SMS 채널 지원 확인")
        void testSmsChannelSupported() {
            log.debug("SMS 채널 지원 확인 테스트 시작");

            assertTrue(notificationService.isChannelSupported(NotificationChannel.SMS));

            log.debug("SMS 채널 지원 확인 테스트 완료");
        }

        @Test
        @DisplayName("카카오 채널 미지원 확인")
        void testKakaoChannelNotSupported() {
            log.debug("카카오 채널 미지원 확인 테스트 시작");

            assertFalse(notificationService.isChannelSupported(NotificationChannel.KAKAO));

            log.debug("카카오 채널 미지원 확인 테스트 완료");
        }

        @Test
        @DisplayName("미지원 채널로 발송 시도 시 예외 발생")
        void testUnsupportedChannelThrowsException() {
            log.debug("미지원 채널 예외 테스트 시작");

            assertThrows(IllegalArgumentException.class, () ->
                    notificationService.send("test", NotificationChannel.KAKAO, "테스트")
            );

            log.debug("미지원 채널 예외 테스트 완료");
        }
    }

    @Nested
    @DisplayName("발송자 구현체 테스트")
    class SenderImplementationTests {

        @Autowired
        private List<NotificationSender> senders;

        @Test
        @DisplayName("이메일 발송자가 템플릿 지원")
        void testEmailSenderSupportsTemplate() {
            log.debug("이메일 템플릿 지원 테스트 시작");

            NotificationSender emailSender = senders.stream()
                    .filter(s -> s.getChannel() == NotificationChannel.EMAIL)
                    .findFirst()
                    .orElseThrow();

            assertTrue(emailSender.supportsTemplate());

            log.debug("이메일 템플릿 지원 테스트 완료");
        }

        @Test
        @DisplayName("SMS 발송자가 템플릿 미지원")
        void testSmsSenderDoesNotSupportTemplate() {
            log.debug("SMS 템플릿 미지원 테스트 시작");

            NotificationSender smsSender = senders.stream()
                    .filter(s -> s.getChannel() == NotificationChannel.SMS)
                    .findFirst()
                    .orElseThrow();

            assertFalse(smsSender.supportsTemplate());

            log.debug("SMS 템플릿 미지원 테스트 완료");
        }

        @Test
        @DisplayName("모든 발송자의 채널이 고유함")
        void testAllSendersHaveUniqueChannels() {
            log.debug("발송자 채널 고유성 테스트 시작");

            long distinctChannelCount = senders.stream()
                    .map(NotificationSender::getChannel)
                    .distinct()
                    .count();

            assertEquals(senders.size(), distinctChannelCount);

            log.debug("발송자 채널 고유성 테스트 완료");
        }
    }
}
