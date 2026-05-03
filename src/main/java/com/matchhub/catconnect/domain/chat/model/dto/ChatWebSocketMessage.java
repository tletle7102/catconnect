package com.matchhub.catconnect.domain.chat.model.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatWebSocketMessage {

    private String type;
    private Object payload;

    public static ChatWebSocketMessage message(ChatMessageResponseDTO dto) {
        return ChatWebSocketMessage.builder()
                .type("MESSAGE")
                .payload(dto)
                .build();
    }

    public static ChatWebSocketMessage readReceipt(Long userId, Long lastReadMessageId) {
        return ChatWebSocketMessage.builder()
                .type("READ_RECEIPT")
                .payload(new ReadReceiptPayload(userId, lastReadMessageId))
                .build();
    }

    public static ChatWebSocketMessage error(String code, String message) {
        return ChatWebSocketMessage.builder()
                .type("ERROR")
                .payload(new ErrorPayload(code, message))
                .build();
    }

    public record ReadReceiptPayload(Long userId, Long lastReadMessageId) {}
    public record ErrorPayload(String code, String message) {}
}
