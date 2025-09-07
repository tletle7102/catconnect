package com.matchhub.catconnect.domain.board.model.dto;

import com.matchhub.catconnect.domain.comment.model.dto.CommentResponseDTO;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class BoardResponseDTO {
    private Long id;
    private String title;
    private String content;
    private String author;
    private LocalDateTime createdDttm;
    private LocalDateTime updatedDttm;
    private int likeCount;
    private List<CommentResponseDTO> comments;
}
