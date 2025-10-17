package com.matchhub.catconnect.domain.comment.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CommentResponseDTO {
    private Long id;
    private String content;
    private String author;
    private LocalDateTime createdDttm;
    private Long boardId;
}
