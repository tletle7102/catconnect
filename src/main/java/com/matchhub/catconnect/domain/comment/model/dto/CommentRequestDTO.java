package com.matchhub.catconnect.domain.comment.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentRequestDTO {
    @NotBlank(message = "댓글 내용은 필수입니다.")
    private String content;
}
