package com.matchhub.catconnect.domain.board.model.dto;

import com.matchhub.catconnect.domain.board.model.enums.BoardCategory;
import com.matchhub.catconnect.domain.board.model.enums.BoardPermissionLevel;
import com.matchhub.catconnect.domain.comment.model.dto.CommentResponseDTO;
import com.matchhub.catconnect.domain.like.model.dto.LikeResponseDTO;
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
    private BoardCategory category;
    private String categoryDisplayName;
    private int viewCount;
    private int likeCount;
    private boolean blinded;
    private BoardPermissionLevel readPermission;
    private BoardPermissionLevel writePermission;
    private boolean ownerReadOnly;
    private List<CommentResponseDTO> comments;
    private List<LikeResponseDTO> likes;
}
