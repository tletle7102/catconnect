package com.matchhub.catconnect.domain.block.model.dto;

import com.matchhub.catconnect.domain.block.model.entity.UserBlock;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BlockResponseDTO {

    private Long userId;
    private String username;
    private LocalDateTime blockedAt;

    public static BlockResponseDTO from(UserBlock block) {
        return BlockResponseDTO.builder()
                .userId(block.getBlocked().getId())
                .username(block.getBlocked().getUsername())
                .blockedAt(block.getCreatedDttm())
                .build();
    }
}
