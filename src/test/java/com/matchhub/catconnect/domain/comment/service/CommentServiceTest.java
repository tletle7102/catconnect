package com.matchhub.catconnect.domain.comment.service;

import com.matchhub.catconnect.domain.board.model.dto.BoardRequestDTO;
import com.matchhub.catconnect.domain.board.model.dto.BoardResponseDTO;
import com.matchhub.catconnect.domain.board.service.BoardService;
import com.matchhub.catconnect.domain.comment.model.dto.CommentRequestDTO;
import com.matchhub.catconnect.domain.comment.model.dto.CommentResponseDTO;
import com.matchhub.catconnect.domain.comment.repository.CommentRepository;
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
 * CommentService의 통합 테스트 클래스
 * 댓글 관련 비즈니스 로직을 테스트하며, 실제 H2 DB와 연동
 */
@DisplayName("CommentService 테스트")
@SpringBootTest
class CommentServiceTest {

    private static final Logger log = LoggerFactory.getLogger(CommentServiceTest.class);

    @Autowired
    private CommentService commentService;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private BoardService boardService;

    private BoardResponseDTO testBoard;

    @BeforeEach
    void setUp() {
        log.debug("테스트 설정 시작");

        // 댓글 데이터 정리
        commentRepository.deleteAll();

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

        // 댓글 데이터 정리
        commentRepository.deleteAll();

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
    @DisplayName("댓글 CRUD 테스트")
    class CommentCrudTests {

        @Test
        @DisplayName("전체 댓글 조회 성공")
        void testGetAllComments() {
            log.debug("전체 댓글 조회 테스트 시작");

            // 댓글 추가
            CommentRequestDTO requestDTO = new CommentRequestDTO();
            requestDTO.setContent("Test Comment");
            commentService.addComment(testBoard.getId(), requestDTO, "testUser");

            // 전체 댓글 조회
            List<CommentResponseDTO> comments = commentService.getAllComments();

            // 댓글 목록 확인
            assertFalse(comments.isEmpty());
            assertTrue(comments.stream().anyMatch(comment -> comment.getContent().equals("Test Comment")));

            log.debug("전체 댓글 조회 테스트 완료");
        }

        @Test
        @DisplayName("댓글 추가 성공")
        void testAddComment() {
            log.debug("댓글 추가 테스트 시작");

            // 댓글 추가
            CommentRequestDTO requestDTO = new CommentRequestDTO();
            requestDTO.setContent("Test Comment");
            commentService.addComment(testBoard.getId(), requestDTO, "testUser");

            // DB에서 댓글 확인
            List<CommentResponseDTO> comments = commentService.getAllComments();
            assertFalse(comments.isEmpty());
            assertEquals("Test Comment", comments.get(0).getContent());

            log.debug("댓글 추가 테스트 완료");
        }

        @Test
        @DisplayName("댓글 삭제 성공")
        void testDeleteComment() {
            log.debug("댓글 삭제 테스트 시작");

            // 댓글 추가
            CommentRequestDTO requestDTO = new CommentRequestDTO();
            requestDTO.setContent("Test Comment");
            commentService.addComment(testBoard.getId(), requestDTO, "testUser");
            CommentResponseDTO comment = commentService.getAllComments().get(0);

            // 댓글 삭제
            commentService.deleteComment(comment.getId());

            // DB에서 댓글 삭제 확인
            assertFalse(commentRepository.existsById(comment.getId()));

            log.debug("댓글 삭제 테스트 완료");
        }

        @Test
        @DisplayName("다중 댓글 삭제 성공")
        void testDeleteComments() {
            log.debug("다중 댓글 삭제 테스트 시작");

            // 댓글 추가
            CommentRequestDTO requestDTO = new CommentRequestDTO();
            requestDTO.setContent("Test Comment");
            commentService.addComment(testBoard.getId(), requestDTO, "testUser");
            CommentResponseDTO comment = commentService.getAllComments().get(0);

            // 다중 삭제
            commentService.deleteComments(List.of(comment.getId()));

            // DB에서 댓글 삭제 확인
            assertFalse(commentRepository.existsById(comment.getId()));

            log.debug("다중 댓글 삭제 테스트 완료");
        }

        @Test
        @DisplayName("다중 댓글 삭제 - 빈 ID 목록 실패")
        void testDeleteCommentsEmptyIds() {
            log.debug("다중 댓글 삭제 빈 ID 테스트 시작");

            // 빈 ID 목록으로 삭제 시도
            AppException exception = assertThrows(AppException.class, () ->
                    commentService.deleteComments(Collections.emptyList())
            );
            assertEquals(ErrorCode.INVALID_REQUEST, exception.getErrorCode());

            log.debug("다중 댓글 삭제 빈 ID 테스트 완료");
        }
    }
}
