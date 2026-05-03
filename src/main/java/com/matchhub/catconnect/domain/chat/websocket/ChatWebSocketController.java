package com.matchhub.catconnect.domain.chat.websocket;

import com.matchhub.catconnect.domain.chat.model.dto.ChatMessageResponseDTO;
import com.matchhub.catconnect.domain.chat.model.dto.ChatWebSocketMessage;
import com.matchhub.catconnect.domain.chat.model.dto.ReadReceiptDTO;
import com.matchhub.catconnect.domain.chat.model.dto.SendMessageDTO;
import com.matchhub.catconnect.domain.chat.service.ChatMessageService;
import com.matchhub.catconnect.domain.chat.service.ChatRoomService;
import com.matchhub.catconnect.global.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketController.class);
    private final ChatMessageService chatMessageService;
    private final ChatRoomService chatRoomService;
    private final SimpMessageSendingOperations messagingTemplate;

    @MessageMapping("/chat/send")
    public void sendMessage(SendMessageDTO dto, Principal principal) {
        String username = principal.getName();
        log.debug("WS 메시지 수신: username={}, roomId={}", username, dto.getRoomId());

        try {
            chatRoomService.validateParticipant(dto.getRoomId(), username);
            ChatMessageResponseDTO response = chatMessageService.sendMessage(username, dto);

            // 채팅방 구독자에게 브로드캐스트
            messagingTemplate.convertAndSend(
                    "/topic/chat/" + dto.getRoomId(),
                    ChatWebSocketMessage.message(response)
            );
        } catch (AppException e) {
            // 에러를 발신자에게만 전달
            messagingTemplate.convertAndSendToUser(
                    username,
                    "/queue/errors",
                    ChatWebSocketMessage.error(e.getErrorCode().getCode(), e.getMessage())
            );
        }
    }

    @MessageMapping("/chat/read")
    public void readReceipt(ReadReceiptDTO dto, Principal principal) {
        String username = principal.getName();
        log.debug("WS 읽음 확인: username={}, roomId={}, lastReadMessageId={}", username, dto.getRoomId(), dto.getLastReadMessageId());

        try {
            chatMessageService.markAsRead(username, dto.getRoomId(), dto.getLastReadMessageId());

            // 상대방에게 읽음 확인 알림
            messagingTemplate.convertAndSend(
                    "/topic/chat/" + dto.getRoomId(),
                    ChatWebSocketMessage.readReceipt(
                            getUserId(username),
                            dto.getLastReadMessageId()
                    )
            );
        } catch (AppException e) {
            log.warn("읽음 확인 실패: {}", e.getMessage());
        }
    }

    private Long getUserId(String username) {
        // Principal에서 직접 가져올 수 없으므로 간단히 해시 사용 (실제로는 캐시 또는 추가 조회 필요)
        // 이 메서드는 READ_RECEIPT에서 userId를 보내기 위한 것
        return (long) username.hashCode();
    }
}
