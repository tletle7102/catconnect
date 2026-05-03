package com.matchhub.catconnect.domain.inbox.model.entity;

import com.matchhub.catconnect.common.model.entity.BaseEntity;
import com.matchhub.catconnect.domain.inbox.model.enums.InboxItemType;
import com.matchhub.catconnect.domain.user.model.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "inbox_items", indexes = {
        @Index(name = "idx_inbox_recipient_updated", columnList = "recipient_id, updatedDttm DESC")
})
@Getter
@NoArgsConstructor
public class InboxItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InboxItemType itemType;

    private Long referenceId;

    @Column(length = 100)
    private String title;

    @Column(length = 200)
    private String preview;

    @Column(length = 500)
    private String linkUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    private LocalDateTime readAt;

    private LocalDateTime pinnedAt;

    private LocalDateTime deletedAt;

    @Builder
    public InboxItem(User recipient, InboxItemType itemType, Long referenceId,
                     String title, String preview, String linkUrl, User sender) {
        this.recipient = recipient;
        this.itemType = itemType;
        this.referenceId = referenceId;
        this.title = title;
        this.preview = preview;
        this.linkUrl = linkUrl;
        this.sender = sender;
    }

    public void updatePreview(String preview, User sender) {
        this.preview = preview;
        this.sender = sender;
        this.readAt = null;
        if (this.deletedAt != null) {
            this.deletedAt = null;
        }
    }

    public void markAsRead() {
        this.readAt = LocalDateTime.now();
    }

    public void pin() {
        this.pinnedAt = LocalDateTime.now();
    }

    public void unpin() {
        this.pinnedAt = null;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isRead() {
        return this.readAt != null;
    }

    public boolean isPinned() {
        return this.pinnedAt != null;
    }
}
