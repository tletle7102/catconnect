package com.matchhub.catconnect.domain.chat.model.dto;

import com.matchhub.catconnect.domain.chat.model.enums.RoomType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatRoomResponseDTO {

    private Long roomId;
    private RoomType roomType;
    private OtherUserDTO otherUser;
    private String lastMessage;
    private Long unreadCount;
    private LocalDateTime updatedAt;

    @Getter
    @Builder
    public static class OtherUserDTO {
        private Long id;
        private String username;
        private String profileImageUrl;
        private boolean deleted;
    }
}
