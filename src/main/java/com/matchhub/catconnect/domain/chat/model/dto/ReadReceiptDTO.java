package com.matchhub.catconnect.domain.chat.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReadReceiptDTO {

    private Long roomId;
    private Long lastReadMessageId;
}
