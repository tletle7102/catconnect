package com.matchhub.catconnect.common.model.dto;

import com.matchhub.catconnect.domain.board.model.dto.BoardResponseDTO;
import com.matchhub.catconnect.domain.comment.model.dto.CommentResponseDTO;
import com.matchhub.catconnect.domain.user.model.dto.UserResponseDTO;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 통합 검색 결과를 담는 DTO
 * 게시글, 댓글, 사용자 검색 결과를 각각의 리스트로 포함함
 * 페이지네이션을 지원하는 버전과 리스트 버전 모두 제공함
 */
@Getter
@Setter
public class SearchResponseDTO {
    private List<BoardResponseDTO> boards;
    private List<CommentResponseDTO> comments;
    private List<UserResponseDTO> users;
    private Page<BoardResponseDTO> boardPage;
    private Page<CommentResponseDTO> commentPage;
    private String keyword;
    private String searchType;
}
