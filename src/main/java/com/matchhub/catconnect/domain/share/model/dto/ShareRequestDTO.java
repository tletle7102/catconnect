package com.matchhub.catconnect.domain.share.model.dto;

import com.matchhub.catconnect.domain.share.model.enums.ShareChannelType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 게시글 공유 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShareRequestDTO {

    private Long boardId;
    private String boardTitle;
    private String boardUrl;
    private ShareChannelType channelType;

    // 이메일 공유 시 사용
    private String recipientEmail;

    // 공유자 정보
    private String senderName;
}
