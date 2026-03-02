package com.matchhub.catconnect.domain.comment.service;

import com.matchhub.catconnect.domain.board.model.dto.BoardRequestDTO;
import com.matchhub.catconnect.domain.board.model.dto.BoardResponseDTO;
import com.matchhub.catconnect.domain.board.service.BoardService;
import com.matchhub.catconnect.domain.comment.model.dto.CommentRequestDTO;
import com.matchhub.catconnect.domain.comment.model.dto.CommentResponseDTO;
import com.matchhub.catconnect.domain.comment.model.entity.Comment;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CommentService의 통합 테스트 클래스
 * 댓글 관련 비즈니스 로직을 테스트하며, 실제 H2 DB와 연동
 */
@DisplayName("CommentService 테스트")
@SpringBootTest
@Transactional
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

        BoardRequestDTO requestDTO = new BoardRequestDTO();
        requestDTO.setTitle("Test Title");
        requestDTO.setContent("Test Content");

        testBoard = boardService.createBoard(requestDTO, "testUser");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "testUser", null, Collections.emptyList()
                )
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    /**
     * 가장 최근 생성된 Comment ID 가져오기 (PostgreSQL 안전)
     */
    private Long getLatestCommentId() {
        return commentRepository.findAll()
                .stream()
                .map(Comment::getId)
                .max(Long::compareTo)
                .orElseThrow();
    }

    private Comment getCommentEntity(Long id) {
        return commentRepository.findById(id).orElseThrow();
    }

    @Nested
    @DisplayName("댓글 CRUD 테스트")
    class CommentCrudTests {

        @Test
        @DisplayName("전체 댓글 조회 성공")
        void testGetAllComments() {

            CommentRequestDTO requestDTO = new CommentRequestDTO();
            requestDTO.setContent("Test Comment");

            commentService.addComment(testBoard.getId(), requestDTO, "testUser");

            List<CommentResponseDTO> comments = commentService.getAllComments();

            assertFalse(comments.isEmpty());
            assertTrue(
                    comments.stream()
                            .anyMatch(c -> c.getContent().equals("Test Comment"))
            );
        }

        @Test
        @DisplayName("댓글 추가 성공")
        void testAddComment() {

            CommentRequestDTO requestDTO = new CommentRequestDTO();
            requestDTO.setContent("Test Comment");

            commentService.addComment(testBoard.getId(), requestDTO, "testUser");

            Long commentId = getLatestCommentId();

            Comment comment = getCommentEntity(commentId);

            assertEquals("Test Comment", comment.getContent());
            assertEquals("testUser", comment.getAuthor());
        }

        @Test
        @DisplayName("댓글 삭제 성공")
        void testDeleteComment() {

            CommentRequestDTO requestDTO = new CommentRequestDTO();
            requestDTO.setContent("Test Comment");

            commentService.addComment(testBoard.getId(), requestDTO, "testUser");

            Long commentId = getLatestCommentId();

            commentService.deleteComment(commentId);

            assertFalse(commentRepository.existsById(commentId));
        }

        @Test
        @DisplayName("다중 댓글 삭제 성공")
        void testDeleteComments() {

            CommentRequestDTO requestDTO = new CommentRequestDTO();
            requestDTO.setContent("Test Comment");

            commentService.addComment(testBoard.getId(), requestDTO, "testUser");

            Long commentId = getLatestCommentId();

            commentService.deleteComments(List.of(commentId));

            assertFalse(commentRepository.existsById(commentId));
        }

        @Test
        @DisplayName("다중 댓글 삭제 - 빈 ID 목록 실패")
        void testDeleteCommentsEmptyIds() {

            AppException exception =
                    assertThrows(AppException.class,
                            () -> commentService.deleteComments(Collections.emptyList()));

            assertEquals(ErrorCode.INVALID_REQUEST, exception.getErrorCode());
        }
    }

    @Nested
    @DisplayName("댓글 권한 검증 테스트")
    class CommentAuthorizationTests {

        @Test
        @DisplayName("본인 댓글 수정 성공")
        void testUpdateCommentByAuthor() {

            CommentRequestDTO requestDTO = new CommentRequestDTO();
            requestDTO.setContent("Original Comment");

            commentService.addComment(testBoard.getId(), requestDTO, "testUser");

            Long commentId = getLatestCommentId();

            commentService.updateComment(
                    commentId,
                    "Updated Comment",
                    "testUser"
            );

            Comment updated = getCommentEntity(commentId);

            assertEquals("Updated Comment", updated.getContent());
        }

        @Test
        @DisplayName("타인 댓글 수정 실패")
        void testUpdateCommentByOther() {

            CommentRequestDTO requestDTO = new CommentRequestDTO();
            requestDTO.setContent("Test Comment");

            commentService.addComment(testBoard.getId(), requestDTO, "testUser");

            Long commentId = getLatestCommentId();

            AppException exception =
                    assertThrows(AppException.class,
                            () -> commentService.updateComment(
                                    commentId,
                                    "Hack",
                                    "otherUser"
                            ));

            assertEquals(ErrorCode.COMMENT_UNAUTHORIZED, exception.getErrorCode());
        }

        @Test
        @DisplayName("본인 댓글 삭제 성공")
        void testDeleteCommentByAuthor() {

            CommentRequestDTO requestDTO = new CommentRequestDTO();
            requestDTO.setContent("Test Comment");

            commentService.addComment(testBoard.getId(), requestDTO, "testUser");

            Long commentId = getLatestCommentId();

            commentService.deleteCommentByAuthor(
                    commentId,
                    "testUser"
            );

            assertFalse(commentRepository.existsById(commentId));
        }

        @Test
        @DisplayName("타인 댓글 삭제 실패")
        void testDeleteCommentByOther() {

            CommentRequestDTO requestDTO = new CommentRequestDTO();
            requestDTO.setContent("Test Comment");

            commentService.addComment(testBoard.getId(), requestDTO, "testUser");

            Long commentId = getLatestCommentId();

            AppException exception =
                    assertThrows(AppException.class,
                            () -> commentService.deleteCommentByAuthor(
                                    commentId,
                                    "otherUser"
                            ));

            assertEquals(ErrorCode.COMMENT_UNAUTHORIZED, exception.getErrorCode());
        }

        @Test
        @DisplayName("존재하지 않는 댓글 수정 실패")
        void testUpdateNonExistentComment() {

            AppException exception =
                    assertThrows(AppException.class,
                            () -> commentService.updateComment(
                                    999999L,
                                    "New Content",
                                    "testUser"
                            ));

            assertEquals(ErrorCode.COMMENT_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        @DisplayName("존재하지 않는 댓글 삭제 실패")
        void testDeleteNonExistentCommentByAuthor() {

            AppException exception =
                    assertThrows(AppException.class,
                            () -> commentService.deleteCommentByAuthor(
                                    999999L,
                                    "testUser"
                            ));

            assertEquals(ErrorCode.COMMENT_NOT_FOUND, exception.getErrorCode());
        }
    }

    @Nested
    @DisplayName("댓글 검색 테스트")
    class CommentSearchTests {

        @Test
        @DisplayName("댓글 내용 검색 성공")
        void testSearchCommentsByContent() {

            CommentRequestDTO requestDTO = new CommentRequestDTO();
            requestDTO.setContent("Test Comment");

            commentService.addComment(testBoard.getId(), requestDTO, "testUser");

            List<CommentResponseDTO> results =
                    commentService.searchComments("Test Comment");

            assertFalse(results.isEmpty());
        }

        @Test
        @DisplayName("댓글 작성자 검색 성공")
        void testSearchCommentsByAuthor() {

            CommentRequestDTO requestDTO = new CommentRequestDTO();
            requestDTO.setContent("Test Comment");

            commentService.addComment(testBoard.getId(), requestDTO, "testUser");

            List<CommentResponseDTO> results =
                    commentService.searchComments("testUser");

            assertFalse(results.isEmpty());
        }

        @Test
        @DisplayName("댓글 검색 결과 없음")
        void testSearchCommentsNoResults() {

            List<CommentResponseDTO> results =
                    commentService.searchComments("no_result_keyword_xyz");

            assertTrue(results.isEmpty());
        }

        @Test
        @DisplayName("댓글 대소문자 구분 없이 검색")
        void testSearchCommentsCaseInsensitive() {

            CommentRequestDTO requestDTO = new CommentRequestDTO();
            requestDTO.setContent("Test Comment");

            commentService.addComment(testBoard.getId(), requestDTO, "testUser");

            List<CommentResponseDTO> results =
                    commentService.searchComments("test comment");

            assertFalse(results.isEmpty());
        }
    }
}
