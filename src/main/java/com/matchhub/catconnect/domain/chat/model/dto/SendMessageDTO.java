package com.matchhub.catconnect.domain.chat.model.dto;

import com.matchhub.catconnect.domain.chat.model.enums.MessageType;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SendMessageDTO {

    private Long roomId;
    private String content;
    private MessageType messageType = MessageType.TEXT;
    private Long fileId;
}
