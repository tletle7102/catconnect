package com.matchhub.catconnect.domain.share.model.dto;

import com.matchhub.catconnect.domain.share.model.enums.ShareChannelType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 게시글 공유 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShareResponseDTO {

    private ShareChannelType channelType;
    private boolean success;
    private String message;

    // 링크 공유 시 사용
    private String shareUrl;

    public static ShareResponseDTO success(ShareChannelType type, String message) {
        return ShareResponseDTO.builder()
                .channelType(type)
                .success(true)
                .message(message)
                .build();
    }

    public static ShareResponseDTO success(ShareChannelType type, String message, String shareUrl) {
        return ShareResponseDTO.builder()
                .channelType(type)
                .success(true)
                .message(message)
                .shareUrl(shareUrl)
                .build();
    }

    public static ShareResponseDTO fail(ShareChannelType type, String message) {
        return ShareResponseDTO.builder()
                .channelType(type)
                .success(false)
                .message(message)
                .build();
    }
}
