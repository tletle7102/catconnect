package com.matchhub.catconnect.domain.like.service;

import com.matchhub.catconnect.domain.board.model.entity.Board;
import com.matchhub.catconnect.domain.board.repository.BoardRepository;
import com.matchhub.catconnect.domain.like.model.dto.LikeResponseDTO;
import com.matchhub.catconnect.domain.like.model.entity.Like;
import com.matchhub.catconnect.domain.like.repository.LikeRepository;
import com.matchhub.catconnect.global.exception.AppException;
import com.matchhub.catconnect.global.exception.Domain;
import com.matchhub.catconnect.global.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class LikeService {

    private static final Logger log = LoggerFactory.getLogger(LikeService.class);
    private final LikeRepository likeRepository;
    private final BoardRepository boardRepository;

    // 생성자를 통한 의존성 주입
    public LikeService(LikeRepository likeRepository, BoardRepository boardRepository) {
        this.likeRepository = likeRepository;
        this.boardRepository = boardRepository;
    }

    @Transactional(readOnly = true)
    public List<LikeResponseDTO> getAllLikes() {
        log.debug("전체 좋아요 조회 요청");
        // DB에서 좋아요 전체 조회
        List<Like> likes = likeRepository.findAll();
        // Entity 리스트를 DTO 리스트로 변환
        return likes.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    // 전체 좋아요 조회 (페이지네이션)
    @Transactional(readOnly = true)
    public Page<LikeResponseDTO> getAllLikes(int page, int size) {
        log.debug("페이지네이션 좋아요 조회 요청: page={}, size={}", page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDttm").descending());
        Page<Like> likePage = likeRepository.findAll(pageable);
        return likePage.map(this::toResponseDTO);
    }

    @Transactional
    public void addLike(Long boardId, String username) {
        log.debug("좋아요 추가 요청: boardId={}, username={}", boardId, username);
        // 중복 좋아요 확인
        if (likeRepository.existsByBoardIdAndUsername(boardId, username)) {
            log.warn("이미 좋아요 존재: boardId={}, username={}", boardId, username);
            throw new AppException(Domain.LIKE, ErrorCode.LIKE_ALREADY_EXISTS);
        }
        // 게시글 존재 확인
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new AppException(Domain.BOARD, ErrorCode.BOARD_NOT_FOUND));
        // 좋아요 엔티티 생성 및 저장
        Like like = new Like(username, board);
        likeRepository.save(like);
        log.debug("좋아요 추가 완료: boardId={}, username={}", boardId, username);
    }

    @Transactional
    public void deleteLikes(List<Long> ids) {
        log.debug("좋아요 다중 삭제 요청: ids={}", ids);
        // ID 목록 유효성 확인
        if (ids == null || ids.isEmpty()) {
            log.warn("삭제할 좋아요 ID 없음");
            throw new AppException(Domain.LIKE, ErrorCode.INVALID_REQUEST, "삭제할 좋아요를 선택하세요.");
        }
        // 일괄 삭제
        likeRepository.deleteAllByIdInBatch(ids);
        log.debug("좋아요 다중 삭제 완료: count={}", ids.size());
    }

    @Transactional
    public void deleteLike(Long id) {
        log.debug("좋아요 개별 삭제 요청: id={}", id);
        // 좋아요 존재 확인
        if (!likeRepository.existsById(id)) {
            log.warn("삭제 대상 좋아요 없음: id={}", id);
            throw new AppException(Domain.LIKE, ErrorCode.LIKE_NOT_FOUND);
        }
        // 좋아요 삭제
        likeRepository.deleteById(id);
        log.debug("좋아요 개별 삭제 완료: id={}", id);
    }

    // Like 엔티티를 LikeResponseDTO로 변환
    private LikeResponseDTO toResponseDTO(Like like) {
        LikeResponseDTO dto = new LikeResponseDTO();
        dto.setId(like.getId());
        dto.setUsername(like.getUsername());
        dto.setBoardId(like.getBoard().getId());
        dto.setCreatedDttm(like.getCreatedDttm());
        return dto;
    }
}
