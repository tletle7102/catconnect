package com.matchhub.catconnect.domain.chat.model.entity;

import com.matchhub.catconnect.domain.user.model.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_room_participants",
        uniqueConstraints = @UniqueConstraint(columnNames = {"chat_room_id", "user_id"}))
@Getter
@NoArgsConstructor
public class ChatRoomParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime joinedAt;

    private LocalDateTime leftAt;

    @Column(nullable = false)
    private Long lastReadMessageId = 0L;

    public ChatRoomParticipant(ChatRoom chatRoom, User user) {
        this.chatRoom = chatRoom;
        this.user = user;
        this.joinedAt = LocalDateTime.now();
    }

    public void leave() {
        this.leftAt = LocalDateTime.now();
    }

    public void rejoin() {
        this.leftAt = null;
    }

    public boolean isActive() {
        return this.leftAt == null;
    }

    public void updateLastReadMessageId(Long messageId) {
        if (messageId > this.lastReadMessageId) {
            this.lastReadMessageId = messageId;
        }
    }
}
