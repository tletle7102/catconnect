package com.matchhub.catconnect.domain.inbox.model.dto;

import com.matchhub.catconnect.domain.inbox.model.entity.InboxItem;
import com.matchhub.catconnect.domain.inbox.model.enums.InboxItemType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class InboxItemResponseDTO {

    private Long id;
    private InboxItemType itemType;
    private Long referenceId;
    private String title;
    private String preview;
    private String linkUrl;
    private Long senderId;
    private String senderName;
    private String senderProfileImage;
    private boolean read;
    private boolean pinned;
    private LocalDateTime updatedAt;

    public static InboxItemResponseDTO from(InboxItem item) {
        return InboxItemResponseDTO.builder()
                .id(item.getId())
                .itemType(item.getItemType())
                .referenceId(item.getReferenceId())
                .title(item.getTitle())
                .preview(item.getPreview())
                .linkUrl(item.getLinkUrl())
                .senderId(item.getSender() != null ? item.getSender().getId() : null)
                .senderName(item.getSender() != null ? item.getSender().getUsername() : null)
                .senderProfileImage(item.getSender() != null ? item.getSender().getProfileImageUrl() : null)
                .read(item.isRead())
                .pinned(item.isPinned())
                .updatedAt(item.getUpdatedDttm())
                .build();
    }
}
