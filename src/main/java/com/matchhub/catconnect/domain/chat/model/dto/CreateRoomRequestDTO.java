package com.matchhub.catconnect.domain.chat.model.dto;

import com.matchhub.catconnect.domain.chat.model.enums.RoomType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateRoomRequestDTO {

    @NotNull(message = "대상 유저 ID는 필수입니다.")
    private Long targetUserId;

    private RoomType roomType = RoomType.DIRECT;
}
