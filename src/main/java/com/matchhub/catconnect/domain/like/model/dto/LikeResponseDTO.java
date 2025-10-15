package com.matchhub.catconnect.domain.like.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class LikeResponseDTO {
    private Long id;
    private String username;
    private Long boardId;
    private LocalDateTime createdDttm;
}
