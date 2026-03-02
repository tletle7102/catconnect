package com.matchhub.catconnect.common.controller;

import com.matchhub.catconnect.domain.board.model.dto.BoardRequestDTO;
import com.matchhub.catconnect.domain.board.model.dto.BoardResponseDTO;
import com.matchhub.catconnect.domain.board.service.BoardService;
import com.matchhub.catconnect.domain.comment.model.dto.CommentRequestDTO;
import com.matchhub.catconnect.domain.comment.service.CommentService;
import com.matchhub.catconnect.domain.user.model.dto.UserResponseDTO;
import com.matchhub.catconnect.domain.user.model.entity.User;
import com.matchhub.catconnect.domain.user.model.enums.Role;
import com.matchhub.catconnect.domain.user.repository.UserRepository;
import com.matchhub.catconnect.domain.user.service.UserService;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * SearchController의 통합 테스트 클래스
 * 검색 API 엔드포인트를 테스트하며, 실제 DB와 연동함
 */
@DisplayName("SearchController 테스트")
@SpringBootTest
@AutoConfigureMockMvc
class SearchControllerTest {

    private static final Logger log = LoggerFactory.getLogger(SearchControllerTest.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BoardService boardService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private BoardResponseDTO testBoard;
    private User testUser;

    @BeforeEach
    void setUp() {
        log.debug("테스트 설정 시작");

        // 기존 데이터 정리
        List<Long> boardIds = boardService.getAllBoards().stream().map(BoardResponseDTO::getId).toList();
        if (!boardIds.isEmpty()) {
            boardService.deleteBoards(boardIds);
        }

        List<Long> userIds = userService.getAllUsers().stream().map(UserResponseDTO::getId).toList();
        if (!userIds.isEmpty()) {
            userService.deleteUsers(userIds);
        }

        // 테스트용 사용자 생성 (Repository 직접 사용)
        testUser = new User("searchTestUser", "search@email.com", passwordEncoder.encode("password123"), Role.USER);
        userRepository.save(testUser);

        // 테스트용 게시글 생성
        BoardRequestDTO boardRequestDTO = new BoardRequestDTO();
        boardRequestDTO.setTitle("검색 테스트 제목");
        boardRequestDTO.setContent("검색 테스트 내용");
        testBoard = boardService.createBoard(boardRequestDTO, "searchTestUser");

        // 테스트용 댓글 생성
        CommentRequestDTO commentRequestDTO = new CommentRequestDTO();
        commentRequestDTO.setContent("검색 테스트 댓글");
        commentService.addComment(testBoard.getId(), commentRequestDTO, "searchTestUser");

        log.debug("테스트 설정 완료: boardId={}, userId={}", testBoard.getId(), testUser.getId());
    }

    @AfterEach
    void tearDown() {
        log.debug("테스트 정리 시작");

        // 전체 데이터 정리
        List<Long> boardIds = boardService.getAllBoards().stream().map(BoardResponseDTO::getId).toList();
        if (!boardIds.isEmpty()) {
            boardService.deleteBoards(boardIds);
        }

        List<Long> userIds = userService.getAllUsers().stream().map(UserResponseDTO::getId).toList();
        if (!userIds.isEmpty()) {
            userService.deleteUsers(userIds);
        }

        log.debug("테스트 정리 완료");
    }

    @Nested
    @DisplayName("검색 API 테스트")
    class SearchApiTests {

        @Test
        @WithMockUser(username = "testUser")
        @DisplayName("전체 검색 성공 - ALL")
        void testSearchAll() throws Exception {
            log.debug("전체 검색 테스트 시작");

            mockMvc.perform(get("/api/search")
                            .param("keyword", "검색")
                            .param("type", "ALL"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.keyword").value("검색"))
                    .andExpect(jsonPath("$.data.searchType").value("ALL"))
                    .andExpect(jsonPath("$.data.boardPage.content").isArray())
                    .andExpect(jsonPath("$.data.commentPage.content").isArray())
                    .andExpect(jsonPath("$.data.users").isEmpty())
                    .andDo(result -> log.debug("전체 검색 응답: {}", result.getResponse().getContentAsString()));

            log.debug("전체 검색 테스트 완료");
        }

        @Test
        @WithMockUser(username = "testUser")
        @DisplayName("게시글 검색 성공 - BOARD")
        void testSearchBoard() throws Exception {
            log.debug("게시글 검색 테스트 시작");

            mockMvc.perform(get("/api/search")
                            .param("keyword", "제목")
                            .param("type", "BOARD"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.keyword").value("제목"))
                    .andExpect(jsonPath("$.data.searchType").value("BOARD"))
                    .andExpect(jsonPath("$.data.boardPage.content").isArray())
                    .andExpect(jsonPath("$.data.boardPage.content[0].title").value("검색 테스트 제목"))
                    .andExpect(jsonPath("$.data.commentPage.content").isEmpty())
                    .andExpect(jsonPath("$.data.users").isEmpty())
                    .andDo(result -> log.debug("게시글 검색 응답: {}", result.getResponse().getContentAsString()));

            log.debug("게시글 검색 테스트 완료");
        }

        @Test
        @WithMockUser(username = "testUser")
        @DisplayName("댓글 검색 성공 - COMMENT")
        void testSearchComment() throws Exception {
            log.debug("댓글 검색 테스트 시작");

            mockMvc.perform(get("/api/search")
                            .param("keyword", "댓글")
                            .param("type", "COMMENT"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.keyword").value("댓글"))
                    .andExpect(jsonPath("$.data.searchType").value("COMMENT"))
                    .andExpect(jsonPath("$.data.boardPage.content").isEmpty())
                    .andExpect(jsonPath("$.data.commentPage.content").isArray())
                    .andExpect(jsonPath("$.data.commentPage.content[0].content").value("검색 테스트 댓글"))
                    .andExpect(jsonPath("$.data.users").isEmpty())
                    .andDo(result -> log.debug("댓글 검색 응답: {}", result.getResponse().getContentAsString()));

            log.debug("댓글 검색 테스트 완료");
        }

        @Test
        @WithMockUser(username = "testUser")
        @DisplayName("검색 결과 없음")
        void testSearchNoResults() throws Exception {
            log.debug("검색 결과 없음 테스트 시작");

            mockMvc.perform(get("/api/search")
                            .param("keyword", "존재하지않는키워드xyz")
                            .param("type", "ALL"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.boardPage.content").isEmpty())
                    .andExpect(jsonPath("$.data.commentPage.content").isEmpty())
                    .andExpect(jsonPath("$.data.users").isEmpty())
                    .andDo(result -> log.debug("검색 결과 없음 응답: {}", result.getResponse().getContentAsString()));

            log.debug("검색 결과 없음 테스트 완료");
        }

        @Test
        @WithMockUser(username = "testUser")
        @DisplayName("유효하지 않은 검색 타입 - 기본값 ALL로 처리")
        void testSearchInvalidType() throws Exception {
            log.debug("유효하지 않은 검색 타입 테스트 시작");

            mockMvc.perform(get("/api/search")
                            .param("keyword", "검색")
                            .param("type", "INVALID_TYPE"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.searchType").value("ALL")) // 기본값으로 처리
                    .andDo(result -> log.debug("유효하지 않은 타입 응답: {}", result.getResponse().getContentAsString()));

            log.debug("유효하지 않은 검색 타입 테스트 완료");
        }
    }
}
