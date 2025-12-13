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

        // 테스트용 댓글 생성 (검색 테스트를 위해)
        CommentRequestDTO commentRequestDTO = new CommentRequestDTO();
        commentRequestDTO.setContent("Test Comment");
        commentService.addComment(testBoard.getId(), commentRequestDTO, "testUser");

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

    @Nested
    @DisplayName("댓글 권한 검증 테스트")
    class CommentAuthorizationTests {

        @Test
        @DisplayName("본인 댓글 수정 성공")
        void testUpdateCommentByAuthor() {
            log.debug("본인 댓글 수정 테스트 시작");

            // 댓글 추가
            CommentRequestDTO requestDTO = new CommentRequestDTO();
            requestDTO.setContent("Original Comment");
            commentService.addComment(testBoard.getId(), requestDTO, "testUser");
            CommentResponseDTO comment = commentService.getAllComments().get(0);

            // 본인이 댓글 수정
            commentService.updateComment(comment.getId(), "Updated Comment", "testUser");

            // 수정된 내용 확인
            CommentResponseDTO updatedComment = commentService.getAllComments().get(0);
            assertEquals("Updated Comment", updatedComment.getContent());

            log.debug("본인 댓글 수정 테스트 완료");
        }

        @Test
        @DisplayName("타인 댓글 수정 실패 - COMMENT_UNAUTHORIZED")
        void testUpdateCommentByOther() {
            log.debug("타인 댓글 수정 테스트 시작");

            // testUser가 댓글 추가
            CommentRequestDTO requestDTO = new CommentRequestDTO();
            requestDTO.setContent("Test Comment");
            commentService.addComment(testBoard.getId(), requestDTO, "testUser");
            CommentResponseDTO comment = commentService.getAllComments().get(0);

            // otherUser가 댓글 수정 시도
            AppException exception = assertThrows(AppException.class, () ->
                    commentService.updateComment(comment.getId(), "Hacked Comment", "otherUser")
            );
            assertEquals(ErrorCode.COMMENT_UNAUTHORIZED, exception.getErrorCode());

            log.debug("타인 댓글 수정 테스트 완료");
        }

        @Test
        @DisplayName("본인 댓글 삭제 성공")
        void testDeleteCommentByAuthor() {
            log.debug("본인 댓글 삭제 테스트 시작");

            // 댓글 추가
            CommentRequestDTO requestDTO = new CommentRequestDTO();
            requestDTO.setContent("Test Comment");
            commentService.addComment(testBoard.getId(), requestDTO, "testUser");
            CommentResponseDTO comment = commentService.getAllComments().get(0);

            // 본인이 댓글 삭제
            commentService.deleteCommentByAuthor(comment.getId(), "testUser");

            // 삭제 확인
            assertFalse(commentRepository.existsById(comment.getId()));

            log.debug("본인 댓글 삭제 테스트 완료");
        }

        @Test
        @DisplayName("타인 댓글 삭제 실패 - COMMENT_UNAUTHORIZED")
        void testDeleteCommentByOther() {
            log.debug("타인 댓글 삭제 테스트 시작");

            // testUser가 댓글 추가
            CommentRequestDTO requestDTO = new CommentRequestDTO();
            requestDTO.setContent("Test Comment");
            commentService.addComment(testBoard.getId(), requestDTO, "testUser");
            CommentResponseDTO comment = commentService.getAllComments().get(0);

            // otherUser가 댓글 삭제 시도
            AppException exception = assertThrows(AppException.class, () ->
                    commentService.deleteCommentByAuthor(comment.getId(), "otherUser")
            );
            assertEquals(ErrorCode.COMMENT_UNAUTHORIZED, exception.getErrorCode());

            log.debug("타인 댓글 삭제 테스트 완료");
        }

        @Test
        @DisplayName("존재하지 않는 댓글 수정 실패 - COMMENT_NOT_FOUND")
        void testUpdateNonExistentComment() {
            log.debug("존재하지 않는 댓글 수정 테스트 시작");

            // 존재하지 않는 댓글 수정 시도
            AppException exception = assertThrows(AppException.class, () ->
                    commentService.updateComment(999L, "New Content", "testUser")
            );
            assertEquals(ErrorCode.COMMENT_NOT_FOUND, exception.getErrorCode());

            log.debug("존재하지 않는 댓글 수정 테스트 완료");
        }

        @Test
        @DisplayName("존재하지 않는 댓글 삭제 실패 - COMMENT_NOT_FOUND")
        void testDeleteNonExistentCommentByAuthor() {
            log.debug("존재하지 않는 댓글 삭제 테스트 시작");

            // 존재하지 않는 댓글 삭제 시도
            AppException exception = assertThrows(AppException.class, () ->
                    commentService.deleteCommentByAuthor(999L, "testUser")
            );
            assertEquals(ErrorCode.COMMENT_NOT_FOUND, exception.getErrorCode());

            log.debug("존재하지 않는 댓글 삭제 테스트 완료");
        }
    }

    @Nested
    @DisplayName("댓글 검색 테스트")
    class CommentSearchTests {

        @Test
        @DisplayName("댓글 내용으로 검색 성공")
        void testSearchCommentsByContent() {
            log.debug("댓글 내용 검색 테스트 시작");

            // 내용으로 검색
            List<CommentResponseDTO> results = commentService.searchComments("Test Comment");

            // 검색 결과 확인
            assertFalse(results.isEmpty());
            assertTrue(results.stream().anyMatch(comment -> comment.getContent().contains("Test Comment")));

            log.debug("댓글 내용 검색 테스트 완료");
        }

        @Test
        @DisplayName("댓글 작성자로 검색 성공")
        void testSearchCommentsByAuthor() {
            log.debug("댓글 작성자 검색 테스트 시작");

            // 작성자로 검색
            List<CommentResponseDTO> results = commentService.searchComments("testUser");

            // 검색 결과 확인
            assertFalse(results.isEmpty());
            assertTrue(results.stream().allMatch(comment -> comment.getAuthor().equals("testUser")));

            log.debug("댓글 작성자 검색 테스트 완료");
        }

        @Test
        @DisplayName("댓글 검색 결과 없음")
        void testSearchCommentsNoResults() {
            log.debug("댓글 검색 결과 없음 테스트 시작");

            // 존재하지 않는 키워드로 검색
            List<CommentResponseDTO> results = commentService.searchComments("존재하지않는키워드xyz");

            // 검색 결과가 비어있는지 확인
            assertTrue(results.isEmpty());

            log.debug("댓글 검색 결과 없음 테스트 완료");
        }

        @Test
        @DisplayName("댓글 대소문자 구분 없이 검색")
        void testSearchCommentsCaseInsensitive() {
            log.debug("댓글 대소문자 구분 없이 검색 테스트 시작");

            // 대소문자 다르게 검색
            List<CommentResponseDTO> results = commentService.searchComments("test comment");

            // 검색 결과 확인
            assertFalse(results.isEmpty());

            log.debug("댓글 대소문자 구분 없이 검색 테스트 완료");
        }
    }
}
