package com.matchhub.catconnect.domain.chat.model.dto;

import com.matchhub.catconnect.domain.chat.model.entity.ChatMessage;
import com.matchhub.catconnect.domain.chat.model.enums.MessageType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatMessageResponseDTO {

    private Long messageId;
    private Long roomId;
    private Long senderId;
    private String senderName;
    private String senderProfileImage;
    private String content;
    private MessageType messageType;
    private Long fileId;
    private String fileUrl;
    private LocalDateTime createdAt;

    public static ChatMessageResponseDTO from(ChatMessage message) {
        return ChatMessageResponseDTO.builder()
                .messageId(message.getId())
                .roomId(message.getChatRoom().getId())
                .senderId(message.getSender() != null ? message.getSender().getId() : null)
                .senderName(message.getSender() != null ? message.getSender().getUsername() : null)
                .senderProfileImage(message.getSender() != null ? message.getSender().getProfileImageUrl() : null)
                .content(message.getContent())
                .messageType(message.getMessageType())
                .fileId(message.getFileId())
                .fileUrl(message.getFileId() != null ? "/api/files/download/" + message.getFileId() : null)
                .createdAt(message.getCreatedDttm())
                .build();
    }
}
