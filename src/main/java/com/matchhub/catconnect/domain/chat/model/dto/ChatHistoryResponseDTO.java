package com.matchhub.catconnect.domain.chat.model.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ChatHistoryResponseDTO {

    private List<ChatMessageResponseDTO> messages;
    private boolean hasMore;
    private Long nextCursor;
}
