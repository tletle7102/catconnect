package com.matchhub.nyangvil.domain.chat.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReadReceiptDTO {

    private Long roomId;
    private Long lastReadMessageId;
}
