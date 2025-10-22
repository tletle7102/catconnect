package com.matchhub.catconnect.domain.like.service;

import com.matchhub.catconnect.domain.board.model.dto.BoardRequestDTO;
import com.matchhub.catconnect.domain.board.model.dto.BoardResponseDTO;
import com.matchhub.catconnect.domain.board.service.BoardService;
import com.matchhub.catconnect.domain.like.model.dto.LikeResponseDTO;
import com.matchhub.catconnect.domain.like.repository.LikeRepository;
import com.matchhub.catconnect.global.exception.AppException;
import com.matchhub.catconnect.global.exception.ErrorCode;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LikeService의 통합 테스트 클래스
 * 좋아요 관련 비즈니스 로직을 테스트하며, 실제 H2 DB와 연동
 */
@DisplayName("LikeService 테스트")
@SpringBootTest
class LikeServiceTest {

    private static final Logger log = LoggerFactory.getLogger(LikeServiceTest.class);

    @Autowired
    private LikeService likeService;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private BoardService boardService;

    private BoardResponseDTO testBoard;

    @BeforeEach
    void setUp() {
        log.debug("테스트 설정 시작");

        // 좋아요 데이터 정리
        likeRepository.deleteAll();

        // 기존 게시글 데이터 정리 (필요 시)
        List<Long> boardIds = boardService.getAllBoards().stream().map(BoardResponseDTO::getId).toList();
        if (!boardIds.isEmpty()) {
            boardService.deleteBoards(boardIds);
            log.debug("기존 게시글 정리 완료: count={}", boardIds.size());
        }

        // 테스트용 게시글 생성
        BoardRequestDTO requestDTO = new BoardRequestDTO();
        requestDTO.setTitle("Test Title");
        requestDTO.setContent("Test Content");
        testBoard = boardService.createBoard(requestDTO, "testUser");

        // 인증 정보 설정
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("testUser", null, Collections.emptyList())
        );

        log.debug("테스트 설정 완료: boardId={}", testBoard.getId());
    }

    @AfterEach
    void tearDown() {
        log.debug("테스트 정리 시작");

        // 좋아요 데이터 정리
        likeRepository.deleteAll();

        // 테스트용 게시글 정리
        List<Long> boardIds = boardService.getAllBoards().stream().map(BoardResponseDTO::getId).toList();
        if (!boardIds.isEmpty()) {
            boardService.deleteBoards(boardIds);
            log.debug("게시글 정리 완료: count={}", boardIds.size());
        }

        // 인증 정보 정리
        SecurityContextHolder.clearContext();

        log.debug("테스트 정리 완료");
    }

    @Nested
    @DisplayName("좋아요 CRUD 테스트")
    class LikeCrudTests {

        @Test
        @DisplayName("전체 좋아요 조회 성공")
        void testGetAllLikes() {
            log.debug("전체 좋아요 조회 테스트 시작");

            // 좋아요 추가
            likeService.addLike(testBoard.getId(), "testUser");

            // 전체 좋아요 조회
            List<LikeResponseDTO> likes = likeService.getAllLikes();

            // 좋아요 목록 확인
            assertFalse(likes.isEmpty());
            assertTrue(likes.stream().anyMatch(like -> like.getBoardId().equals(testBoard.getId()) && like.getUsername().equals("testUser")));

            log.debug("전체 좋아요 조회 테스트 완료");
        }

        @Test
        @DisplayName("좋아요 추가 성공")
        void testAddLike() {
            log.debug("좋아요 추가 테스트 시작");

            // 좋아요 추가
            likeService.addLike(testBoard.getId(), "testUser");

            // DB에서 좋아요 확인
            assertTrue(likeRepository.existsByBoardIdAndUsername(testBoard.getId(), "testUser"));

            log.debug("좋아요 추가 테스트 완료");
        }

        @Test
        @DisplayName("중복 좋아요 추가 실패")
        void testAddDuplicateLike() {
            log.debug("중복 좋아요 추가 테스트 시작");

            // 첫 번째 좋아요 추가
            likeService.addLike(testBoard.getId(), "testUser");

            // 중복 좋아요 시도
            AppException exception = assertThrows(AppException.class, () ->
                    likeService.addLike(testBoard.getId(), "testUser")
            );
            assertEquals(ErrorCode.LIKE_ALREADY_EXISTS, exception.getErrorCode());

            log.debug("중복 좋아요 추가 테스트 완료");
        }

        @Test
        @DisplayName("좋아요 삭제 성공")
        void testDeleteLike() {
            log.debug("좋아요 삭제 테스트 시작");

            // 좋아요 추가
            likeService.addLike(testBoard.getId(), "testUser");
            LikeResponseDTO like = likeService.getAllLikes().get(0);

            // 좋아요 삭제
            likeService.deleteLike(like.getId());

            // DB에서 좋아요 삭제 확인
            assertFalse(likeRepository.existsById(like.getId()));

            log.debug("좋아요 삭제 테스트 완료");
        }

        @Test
        @DisplayName("다중 좋아요 삭제 성공")
        void testDeleteLikes() {
            log.debug("다중 좋아요 삭제 테스트 시작");

            // 좋아요 추가
            likeService.addLike(testBoard.getId(), "testUser");
            LikeResponseDTO like = likeService.getAllLikes().get(0);

            // 다중 삭제
            likeService.deleteLikes(List.of(like.getId()));

            // DB에서 좋아요 삭제 확인
            assertFalse(likeRepository.existsById(like.getId()));

            log.debug("다중 좋아요 삭제 테스트 완료");
        }

        @Test
        @DisplayName("다중 좋아요 삭제 - 빈 ID 목록 실패")
        void testDeleteLikesEmptyIds() {
            log.debug("다중 좋아요 삭제 빈 ID 테스트 시작");

            // 빈 ID 목록으로 삭제 시도
            AppException exception = assertThrows(AppException.class, () ->
                    likeService.deleteLikes(Collections.emptyList())
            );
            assertEquals(ErrorCode.INVALID_REQUEST, exception.getErrorCode());

            log.debug("다중 좋아요 삭제 빈 ID 테스트 완료");
        }
    }
}
