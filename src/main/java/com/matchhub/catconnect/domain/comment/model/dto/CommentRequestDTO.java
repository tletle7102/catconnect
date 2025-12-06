package com.matchhub.catconnect.domain.comment.model.dto;

import com.matchhub.catconnect.global.validation.RestrictedString;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentRequestDTO {
    @NotBlank(message = "댓글 내용은 필수입니다.")
    @Size(max = 500, message = "댓글은 500자 이내여야 합니다.")
    @RestrictedString
    private String content;
}
