package com.matchhub.catconnect.common.model.dto;

import com.matchhub.catconnect.domain.board.model.dto.BoardResponseDTO;
import com.matchhub.catconnect.domain.comment.model.dto.CommentResponseDTO;
import com.matchhub.catconnect.domain.user.model.dto.UserResponseDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 통합 검색 결과를 담는 DTO
 * 게시글, 댓글, 사용자 검색 결과를 각각의 리스트로 포함함
 */
@Getter
@Setter
public class SearchResponseDTO {
    private List<BoardResponseDTO> boards;
    private List<CommentResponseDTO> comments;
    private List<UserResponseDTO> users;
    private String keyword;
    private String searchType;
}
