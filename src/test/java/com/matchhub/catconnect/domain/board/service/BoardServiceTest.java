package com.matchhub.catconnect.domain.board.service;

import com.matchhub.catconnect.domain.board.model.dto.BoardRequestDTO;
import com.matchhub.catconnect.domain.board.model.dto.BoardResponseDTO;
import com.matchhub.catconnect.domain.board.repository.BoardRepository;
import com.matchhub.catconnect.domain.comment.model.dto.CommentRequestDTO;
import com.matchhub.catconnect.domain.comment.model.entity.Comment;
import com.matchhub.catconnect.domain.comment.repository.CommentRepository;
import com.matchhub.catconnect.domain.comment.service.CommentService;
import com.matchhub.catconnect.domain.like.repository.LikeRepository;
import com.matchhub.catconnect.domain.like.service.LikeService;
import com.matchhub.catconnect.global.exception.AppException;
import com.matchhub.catconnect.global.exception.ErrorCode;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
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

    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;

    private BoardResponseDTO testBoard;
    private BoardResponseDTO testBoard2;

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
        testBoard = boardService.createBoard(requestDTO, "testUser");

        // 다중 삭제 테스트용 두 번째 게시글 생성
        requestDTO.setTitle("Test Title 2");
        requestDTO.setContent("Test Content 2");
        testBoard2 = boardService.createBoard(requestDTO, "testUser");

        // 인증 정보 설정 (현재 사용자: testUser)
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("testUser", null, Collections.emptyList())
        );

        log.debug("테스트 설정 완료: boardId1={}, boardId2={}", testBoard.getId(), testBoard2.getId());
    }

    @AfterEach
    void tearDown() {
        log.debug("테스트 정리 시작");
        try {
            // 전체 데이터 정리
            boardRepository.deleteAll();
            likeRepository.deleteAll();
            commentRepository.deleteAll();
            SecurityContextHolder.clearContext();
            log.debug("테스트 정리 완료");
        } catch (Exception e) {
            log.debug("테스트 정리 실패: {}", e.getMessage());
        }
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

            // 게시글이 비어있지 않고, 생성한 게시글들 포함 확인
            assertFalse(boards.isEmpty());
            assertTrue(boards.stream().anyMatch(board -> board.getId().equals(testBoard.getId()) && board.getTitle().equals("Test Title")));
            assertTrue(boards.stream().anyMatch(board -> board.getId().equals(testBoard2.getId()) && board.getTitle().equals("Test Title 2")));

            log.debug("전체 게시글 조회 테스트 완료");
        }

        @Test
        @DisplayName("페이지네이션 게시글 조회 성공")
        void testGetAllBoardsWithPagination() {
            log.debug("페이지네이션 게시글 조회 테스트 시작");

            // 페이지네이션으로 게시글 조회 (첫 페이지, 10개씩)
            Page<BoardResponseDTO> boardPage = boardService.getAllBoards(0, 10);

            // 페이지 정보 확인
            assertNotNull(boardPage);
            assertFalse(boardPage.isEmpty());
            assertEquals(0, boardPage.getNumber()); // 현재 페이지 번호
            assertEquals(2, boardPage.getTotalElements()); // 전체 게시글 수
            assertEquals(1, boardPage.getTotalPages()); // 전체 페이지 수

            // 게시글 내용 확인
            assertTrue(boardPage.getContent().stream().anyMatch(board -> board.getTitle().equals("Test Title")));
            assertTrue(boardPage.getContent().stream().anyMatch(board -> board.getTitle().equals("Test Title 2")));

            log.debug("페이지네이션 게시글 조회 테스트 완료");
        }

        @Test
        @DisplayName("페이지네이션 최신순 정렬 확인")
        void testGetAllBoardsWithPaginationSorting() {
            log.debug("페이지네이션 정렬 테스트 시작");

            // 페이지네이션으로 게시글 조회
            Page<BoardResponseDTO> boardPage = boardService.getAllBoards(0, 10);

            // 최신순 정렬 확인 (Test Title 2가 먼저 나와야 함)
            List<BoardResponseDTO> content = boardPage.getContent();
            assertEquals("Test Title 2", content.get(0).getTitle());
            assertEquals("Test Title", content.get(1).getTitle());

            log.debug("페이지네이션 정렬 테스트 완료");
        }

        @Test
        @DisplayName("페이지네이션 페이지 크기 확인")
        void testGetAllBoardsWithPaginationSize() {
            log.debug("페이지네이션 크기 테스트 시작");

            // 페이지 크기를 1로 설정하여 조회
            Page<BoardResponseDTO> boardPage = boardService.getAllBoards(0, 1);

            // 페이지 정보 확인
            assertEquals(1, boardPage.getSize()); // 페이지 크기
            assertEquals(1, boardPage.getNumberOfElements()); // 현재 페이지 요소 수
            assertEquals(2, boardPage.getTotalElements()); // 전체 요소 수
            assertEquals(2, boardPage.getTotalPages()); // 전체 페이지 수

            log.debug("페이지네이션 크기 테스트 완료");
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

            // DB에서 수정 확인
            BoardResponseDTO dbBoard = boardService.getBoardById(testBoard.getId());
            assertEquals("Updated Title", dbBoard.getTitle());

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
        @DisplayName("게시글 단일 삭제 성공")
        void testDeleteBoard() {
            log.debug("게시글 단일 삭제 테스트 시작");

            // 댓글 추가 (cascade 삭제 테스트)
            CommentRequestDTO commentDTO = new CommentRequestDTO();
            commentDTO.setContent("Test Comment");
            commentService.addComment(testBoard.getId(), commentDTO, "testUser");

            // 게시글 삭제
            boardService.deleteBoard(testBoard.getId());

            // 게시글과 댓글이 삭제되었는지 확인
            assertFalse(boardRepository.existsById(testBoard.getId()));
            assertTrue(commentRepository.findAllByBoardId(testBoard.getId()).isEmpty());

            log.debug("게시글 단일 삭제 테스트 완료");
        }

        @Test
        @DisplayName("게시글 다중 삭제 성공")
        void testDeleteBoards() {
            log.debug("게시글 다중 삭제 테스트 시작");

            // 댓글 추가 (cascade 삭제 테스트)
            CommentRequestDTO commentDTO = new CommentRequestDTO();
            commentDTO.setContent("Test Comment");
            commentService.addComment(testBoard.getId(), commentDTO, "testUser");

            // 다중 삭제
            boardService.deleteBoards(List.of(testBoard.getId(), testBoard2.getId()));

            // 게시글과 댓글이 삭제되었는지 확인
            assertFalse(boardRepository.existsById(testBoard.getId()));
            assertFalse(boardRepository.existsById(testBoard2.getId()));
            assertTrue(commentRepository.findAllByBoardId(testBoard.getId()).isEmpty());

            log.debug("게시글 다중 삭제 테스트 완료");
        }

        @Test
        @DisplayName("게시글 다중 삭제 - 게시글 없음 실패")
        void testDeleteBoardsNotFound() {
            log.debug("게시글 다중 삭제 게시글 없음 테스트 시작");

            // 존재하지 않는 ID 포함
            AppException exception = assertThrows(AppException.class, () ->
                    boardService.deleteBoards(List.of(testBoard.getId(), 999L))
            );
            assertEquals(ErrorCode.BOARD_NOT_FOUND, exception.getErrorCode());

            log.debug("게시글 다중 삭제 게시글 없음 테스트 완료");
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
            likeService.addLike(testBoard.getId(), "testUser");

            // 좋아요가 DB에 저장되었는지 확인
            assertTrue(likeRepository.existsByBoardIdAndUsername(testBoard.getId(), "testUser"));

            log.debug("좋아요 추가 테스트 완료");
        }

        @Test
        @DisplayName("중복 좋아요 추가 실패")
        void testAddDuplicateLike() {
            log.debug("중복 좋아요 추가 테스트 시작");

            // 먼저 좋아요 추가
            likeService.addLike(testBoard.getId(), "testUser");

            // 같은 유저가 다시 좋아요 시도
            AppException exception = assertThrows(AppException.class, () ->
                    likeService.addLike(testBoard.getId(), "testUser")
            );
            assertEquals(ErrorCode.LIKE_ALREADY_EXISTS, exception.getErrorCode());

            log.debug("중복 좋아요 추가 테스트 완료");
        }

        @Test
        @DisplayName("댓글 추가 성공")
        void testAddComment() {
            log.debug("댓글 추가 테스트 시작");

            // 댓글 요청 DTO 생성
            CommentRequestDTO commentDTO = new CommentRequestDTO();
            commentDTO.setContent("Test Comment");

            // 댓글 추가
            commentService.addComment(testBoard.getId(), commentDTO, "testUser");

            // 댓글이 DB에 저장되었는지 확인
            List<Comment> comments = commentRepository.findAll();
            assertFalse(comments.isEmpty());
            assertEquals("Test Comment", comments.get(0).getContent());

            log.debug("댓글 추가 테스트 완료");
        }
    }

    @Nested
    @DisplayName("게시글 검색 테스트")
    class BoardSearchTests {

        @Test
        @DisplayName("게시글 제목으로 검색 성공")
        void testSearchBoardsByTitle() {
            log.debug("게시글 제목 검색 테스트 시작");

            // 제목으로 검색
            List<BoardResponseDTO> results = boardService.searchBoards("Test Title");

            // 검색 결과 확인
            assertFalse(results.isEmpty());
            assertTrue(results.stream().anyMatch(board -> board.getTitle().contains("Test Title")));

            log.debug("게시글 제목 검색 테스트 완료");
        }

        @Test
        @DisplayName("게시글 내용으로 검색 성공")
        void testSearchBoardsByContent() {
            log.debug("게시글 내용 검색 테스트 시작");

            // 내용으로 검색
            List<BoardResponseDTO> results = boardService.searchBoards("Test Content");

            // 검색 결과 확인
            assertFalse(results.isEmpty());
            assertTrue(results.stream().anyMatch(board -> board.getContent().contains("Test Content")));

            log.debug("게시글 내용 검색 테스트 완료");
        }

        @Test
        @DisplayName("게시글 작성자로 검색 성공")
        void testSearchBoardsByAuthor() {
            log.debug("게시글 작성자 검색 테스트 시작");

            // 작성자로 검색
            List<BoardResponseDTO> results = boardService.searchBoards("testUser");

            // 검색 결과 확인
            assertFalse(results.isEmpty());
            assertTrue(results.stream().allMatch(board -> board.getAuthor().equals("testUser")));

            log.debug("게시글 작성자 검색 테스트 완료");
        }

        @Test
        @DisplayName("게시글 검색 결과 없음")
        void testSearchBoardsNoResults() {
            log.debug("게시글 검색 결과 없음 테스트 시작");

            // 존재하지 않는 키워드로 검색
            List<BoardResponseDTO> results = boardService.searchBoards("존재하지않는키워드xyz");

            // 검색 결과가 비어있는지 확인
            assertTrue(results.isEmpty());

            log.debug("게시글 검색 결과 없음 테스트 완료");
        }

        @Test
        @DisplayName("게시글 대소문자 구분 없이 검색")
        void testSearchBoardsCaseInsensitive() {
            log.debug("게시글 대소문자 구분 없이 검색 테스트 시작");

            // 대소문자 다르게 검색
            List<BoardResponseDTO> results = boardService.searchBoards("test title");

            // 검색 결과 확인
            assertFalse(results.isEmpty());

            log.debug("게시글 대소문자 구분 없이 검색 테스트 완료");
        }
    }
}
