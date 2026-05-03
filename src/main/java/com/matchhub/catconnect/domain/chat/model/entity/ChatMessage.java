package com.matchhub.catconnect.domain.chat.model.entity;

import com.matchhub.catconnect.domain.chat.model.enums.MessageType;
import com.matchhub.catconnect.domain.user.model.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages", indexes = {
        @Index(name = "idx_chat_messages_room_created", columnList = "chat_room_id, id DESC")
})
@Getter
@NoArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MessageType messageType;

    private Long fileId;

    @Column(nullable = false)
    private Boolean isDeleted = false;

    @Column(nullable = false)
    private LocalDateTime createdDttm;

    @PrePersist
    protected void onCreate() {
        this.createdDttm = LocalDateTime.now();
    }

    public ChatMessage(ChatRoom chatRoom, User sender, String content, MessageType messageType, Long fileId) {
        this.chatRoom = chatRoom;
        this.sender = sender;
        this.content = content;
        this.messageType = messageType;
        this.fileId = fileId;
    }

    public static ChatMessage systemMessage(ChatRoom chatRoom, String content) {
        return new ChatMessage(chatRoom, null, content, MessageType.SYSTEM, null);
    }
}
