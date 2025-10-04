package com.matchhub.catconnect.domain.board.service;

import com.matchhub.catconnect.domain.board.model.dto.BoardRequestDTO;
import com.matchhub.catconnect.domain.board.model.dto.BoardResponseDTO;
import com.matchhub.catconnect.domain.board.repository.BoardRepository;
import com.matchhub.catconnect.domain.comment.model.dto.CommentRequestDTO;
import com.matchhub.catconnect.domain.comment.model.entity.Comment;
import com.matchhub.catconnect.domain.comment.repository.CommentRepository;
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

@DisplayName("BoardService 테스트")
@SpringBootTest
class BoardServiceTest {

    private static final Logger log = LoggerFactory.getLogger(BoardServiceTest.class);

    @Autowired
    private BoardService boardService;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private CommentRepository commentRepository;

    private BoardResponseDTO testBoard;

    @BeforeEach
    void setUp() {
        log.debug("테스트 설정 시작");

        // 테스트 전 DB 정리
        boardRepository.deleteAll();
        likeRepository.deleteAll();
        commentRepository.deleteAll();

        // 게시글 생성
        BoardRequestDTO requestDTO = new BoardRequestDTO();
        requestDTO.setTitle("Test Title");
        requestDTO.setContent("Test Content");

        // 게시글 생성 후 저장
        testBoard = boardService.createBoard(requestDTO, "testUser");

        // 인증 정보 설정 (현재 사용자: testUser)
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("testUser", null, Collections.emptyList())
        );

        log.debug("테스트 설정 완료: boardId={}", testBoard.getId());
    }

    @AfterEach
    void tearDown() {
        log.debug("테스트 정리 시작");
        try {
            // 게시글이 남아 있으면 삭제
            if (boardRepository.existsById(testBoard.getId())) {
                boardService.deleteBoard(testBoard.getId());
                log.debug("테스트 정리 완료: boardId={}", testBoard.getId());
            }
        } catch (AppException e) {
            log.debug("테스트 정리: 이미 삭제된 게시글, boardId={}", testBoard.getId());
        }

        // 전체 데이터 정리
        boardRepository.deleteAll();
        likeRepository.deleteAll();
        commentRepository.deleteAll();
        SecurityContextHolder.clearContext();
        log.debug("테스트 정리 완료");
    }

    @Nested
    @DisplayName("게시글 CRUD 테스트")
    class BoardCrudTests {

        @Test
        @DisplayName("전체 게시글 조회 성공")
        void testGetAllBoards() {
            log.debug("전체 게시글 조회 테스트 시작");

            // 전체 게시글 목록 조회
            List<BoardResponseDTO> boards = boardService.getAllBoards();

            // 게시글이 비어있지 않고, 방금 만든 게시글이 포함되어야 함
            assertFalse(boards.isEmpty());
            assertTrue(boards.stream().anyMatch(board -> board.getId().equals(testBoard.getId()) && board.getTitle().equals("Test Title")));

            log.debug("전체 게시글 조회 테스트 완료");
        }

        @Test
        @DisplayName("게시글 상세 조회 성공")
        void testGetBoardById() {
            log.debug("게시글 상세 조회 테스트 시작");

            // 게시글 ID로 상세 조회
            BoardResponseDTO board = boardService.getBoardById(testBoard.getId());

            // 제목과 작성자가 예상대로인지 확인
            assertEquals("Test Title", board.getTitle());
            assertEquals("testUser", board.getAuthor());

            log.debug("게시글 상세 조회 테스트 완료");
        }

        @Test
        @DisplayName("게시글 생성 성공")
        void testCreateBoard() {
            log.debug("게시글 생성 테스트 시작");

            // 새 게시글 요청 데이터 생성
            BoardRequestDTO requestDTO = new BoardRequestDTO();
            requestDTO.setTitle("New Title");
            requestDTO.setContent("New Content");

            // 게시글 생성
            BoardResponseDTO board = boardService.createBoard(requestDTO, "testUser");

            // 게시글이 정상적으로 생성되었는지 검증
            assertNotNull(board.getId());
            assertEquals("New Title", board.getTitle());

            log.debug("게시글 생성 테스트 완료");
        }

        @Test
        @DisplayName("게시글 수정 성공")
        void testUpdateBoard() {
            log.debug("게시글 수정 테스트 시작");

            // 수정 요청 DTO 생성
            BoardRequestDTO requestDTO = new BoardRequestDTO();
            requestDTO.setTitle("Updated Title");
            requestDTO.setContent("Updated Content");

            // 게시글 수정
            BoardResponseDTO updatedBoard = boardService.updateBoard(testBoard.getId(), requestDTO, "testUser");

            // 수정 내용 검증
            assertEquals("Updated Title", updatedBoard.getTitle());
            assertEquals("Updated Content", updatedBoard.getContent());

            log.debug("게시글 수정 테스트 완료");
        }

        @Test
        @DisplayName("게시글 수정 권한 없음 실패")
        void testUpdateBoardUnauthorized() {
            log.debug("게시글 수정 권한 없음 테스트 시작");

            // 다른 사용자로 인증 정보 변경
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken("otherUser", null, Collections.emptyList())
            );

            // 수정 요청 DTO
            BoardRequestDTO requestDTO = new BoardRequestDTO();
            requestDTO.setTitle("Unauthorized Title");
            requestDTO.setContent("Unauthorized Content");

            // 예외 발생 기대
            AppException exception = assertThrows(AppException.class, () ->
                    boardService.updateBoard(testBoard.getId(), requestDTO, "otherUser")
            );
            assertEquals(ErrorCode.BOARD_UNAUTHORIZED, exception.getErrorCode());

            log.debug("게시글 수정 권한 없음 테스트 완료");
        }

        @Test
        @DisplayName("게시글 삭제 성공")
        void testDeleteBoard() {
            log.debug("게시글 삭제 테스트 시작");

            // 게시글 삭제
            boardService.deleteBoard(testBoard.getId());

            // 게시글이 실제로 삭제되었는지 확인
            assertFalse(boardRepository.existsById(testBoard.getId()));

            log.debug("게시글 삭제 테스트 완료");
        }
    }

    @Nested
    @DisplayName("좋아요 및 댓글 테스트")
    class LikeAndCommentTests {

        @Test
        @DisplayName("좋아요 추가 성공")
        void testAddLike() {
            log.debug("좋아요 추가 테스트 시작");

            // 좋아요 추가
            boardService.addLike(testBoard.getId(), "testUser");

            // 좋아요가 DB에 저장되었는지 확인
            assertTrue(likeRepository.existsByBoardIdAndUsername(testBoard.getId(), "testUser"));

            log.debug("좋아요 추가 테스트 완료");
        }

        @Test
        @DisplayName("중복 좋아요 추가 실패")
        void testAddDuplicateLike() {
            log.debug("중복 좋아요 추가 테스트 시작");

            // 먼저 좋아요 추가
            boardService.addLike(testBoard.getId(), "testUser");

            // 같은 유저가 다시 좋아요를 누르면 예외 발생해야 함
            AppException exception = assertThrows(AppException.class, () ->
                    boardService.addLike(testBoard.getId(), "testUser")
            );
            assertEquals(ErrorCode.LIKE_ALREADY_EXISTS, exception.getErrorCode());

            log.debug("중복 좋아요 추가 테스트 완료");
        }

        @Test
        @DisplayName("댓글 추가 성공")
        void testAddComment() {
            log.debug("댓글 추가 테스트 시작");

            // 댓글 요청 DTO 생성
            CommentRequestDTO requestDTO = new CommentRequestDTO();
            requestDTO.setContent("Test Comment");

            // 댓글 추가
            boardService.addComment(testBoard.getId(), requestDTO, "testUser");

            // 댓글이 DB에 저장되었는지 확인
            List<Comment> comments = commentRepository.findAll();
            assertFalse(comments.isEmpty());
            assertEquals("Test Comment", comments.get(0).getContent());

            log.debug("댓글 추가 테스트 완료");
        }
    }
}
