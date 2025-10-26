package com.matchhub.catconnect.domain.board.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matchhub.catconnect.domain.board.model.dto.BoardRequestDTO;
import com.matchhub.catconnect.domain.board.model.dto.BoardResponseDTO;
import com.matchhub.catconnect.domain.board.service.BoardService;
import com.matchhub.catconnect.domain.comment.model.dto.CommentRequestDTO;
import com.matchhub.catconnect.domain.comment.service.CommentService;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("BoardRestController 테스트")
@SpringBootTest // 스프링 애플리케이션 전체를 로드하여 통합 테스트 수행
@AutoConfigureMockMvc // MockMvc 자동 설정 (의존성 주입 가능)
class BoardRestControllerTest {

    private static final Logger log = LoggerFactory.getLogger(BoardRestControllerTest.class);

    @Autowired
    private MockMvc mockMvc; // REST API를 테스트하기 위한 MockMvc

    @Autowired
    private ObjectMapper objectMapper; // 객체를 JSON으로 직렬화/역직렬화하는 Jackson 도구

    @Autowired
    private BoardService boardService; // 게시판 서비스

    @Autowired
    private CommentService commentService; // 댓글 서비스

    private BoardResponseDTO testBoard; // 테스트용 게시글
    private BoardResponseDTO testBoard2; // 다중 삭제 테스트용 추가 게시글

    @BeforeEach
    void setUp() {
        log.debug("테스트 설정 시작");

        // 기존 게시글 데이터 전체 삭제
        List<Long> ids = boardService.getAllBoards().stream().map(BoardResponseDTO::getId).toList();
        if (!ids.isEmpty()) {
            try {
                boardService.deleteBoards(ids);
            } catch (Exception e) {
                log.debug("기존 데이터 정리 실패: {}", e.getMessage());
            }
        }

        // 테스트용 게시글 생성
        BoardRequestDTO requestDTO = new BoardRequestDTO();
        requestDTO.setTitle("Test Title");
        requestDTO.setContent("Test Content");
        testBoard = boardService.createBoard(requestDTO, "testUser");

        // 다중 삭제 테스트용 두 번째 게시글 생성
        requestDTO.setTitle("Test Title 2");
        requestDTO.setContent("Test Content 2");
        testBoard2 = boardService.createBoard(requestDTO, "testUser");

        log.debug("테스트 설정 완료: boardId1={}, boardId2={}", testBoard.getId(), testBoard2.getId());
    }

    @AfterEach
    void tearDown() {
        log.debug("테스트 정리 시작");

        // 테스트용 게시글 삭제
        List<Long> ids = boardService.getAllBoards().stream().map(BoardResponseDTO::getId).toList();
        if (!ids.isEmpty()) {
            try {
                boardService.deleteBoards(ids);
            } catch (Exception e) {
                log.debug("전체 데이터 정리 실패: {}", e.getMessage());
            }
        }

        log.debug("테스트 정리 완료");
    }

    @Nested
    @DisplayName("게시글 API 테스트")
    class BoardApiTests {

        @Test
        @DisplayName("게시글 목록 조회 성공")
        void testGetAllBoards() throws Exception {
            log.debug("게시글 목록 조회 테스트 시작");

            mockMvc.perform(get("/api/boards").contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andExpect(jsonPath("$.data[?(@.id == %d)].title", testBoard.getId()).value("Test Title"))
                    .andExpect(jsonPath("$.data[?(@.id == %d)].title", testBoard2.getId()).value("Test Title 2"))
                    .andDo(result -> log.debug("게시글 목록 조회 응답: {}", result.getResponse().getContentAsString()));

            log.debug("게시글 목록 조회 테스트 완료");
        }

        @Test
        @DisplayName("게시글 상세 조회 성공")
        void testGetBoardById() throws Exception {
            log.debug("게시글 상세 조회 테스트 시작");

            mockMvc.perform(get("/api/boards/" + testBoard.getId()).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.title").value("Test Title"))
                    .andExpect(jsonPath("$.data.comments").isArray())
                    .andDo(result -> log.debug("게시글 상세 조회 응답: {}", result.getResponse().getContentAsString()));

            log.debug("게시글 상세 조회 테스트 완료");
        }

        @Test
        @WithMockUser(username = "testUser")
        @DisplayName("게시글 생성 성공")
        void testCreateBoard() throws Exception {
            log.debug("게시글 생성 테스트 시작");

            BoardRequestDTO requestDTO = new BoardRequestDTO();
            requestDTO.setTitle("New Title");
            requestDTO.setContent("New Content");

            mockMvc.perform(post("/api/boards")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.title").value("New Title"))
                    .andDo(result -> log.debug("게시글 생성 응답: {}", result.getResponse().getContentAsString()));

            log.debug("게시글 생성 테스트 완료");
        }

        @Test
        @WithMockUser(username = "testUser")
        @DisplayName("게시글 수정 성공")
        void testUpdateBoard() throws Exception {
            log.debug("게시글 수정 테스트 시작");

            BoardRequestDTO requestDTO = new BoardRequestDTO();
            requestDTO.setTitle("Updated Title");
            requestDTO.setContent("Updated Content");

            mockMvc.perform(put("/api/boards/" + testBoard.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.title").value("Updated Title"))
                    .andExpect(jsonPath("$.data.content").value("Updated Content"))
                    .andDo(result -> log.debug("게시글 수정 응답: {}", result.getResponse().getContentAsString()));

            log.debug("게시글 수정 테스트 완료");
        }

        @Test
        @WithMockUser(username = "otherUser")
        @DisplayName("게시글 수정 권한 없음 실패")
        void testUpdateBoardUnauthorized() throws Exception {
            log.debug("게시글 수정 권한 없음 테스트 시작");

            BoardRequestDTO requestDTO = new BoardRequestDTO();
            requestDTO.setTitle("Unauthorized Title");
            requestDTO.setContent("Unauthorized Content");

            mockMvc.perform(put("/api/boards/" + testBoard.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.domain").value("BOARD"))
                    .andExpect(jsonPath("$.code").value("BOARD_002"))
                    .andExpect(jsonPath("$.message").value("게시글에 대한 권한이 없습니다."))
                    .andExpect(jsonPath("$.status").value(403))
                    .andDo(result -> log.debug("게시글 수정 권한 없음 응답: {}", result.getResponse().getContentAsString()));

            log.debug("게시글 수정 권한 없음 테스트 완료");
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("게시글 다중 삭제 성공")
        void testDeleteBoards() throws Exception {
            log.debug("게시글 다중 삭제 테스트 시작");

            // 댓글 추가 (cascade 삭제 테스트)
            CommentRequestDTO commentDTO = new CommentRequestDTO();
            commentDTO.setContent("Test Comment");
            commentService.addComment(testBoard.getId(), commentDTO, "testUser");

            // 삭제 요청 페이로드 구성
            Map<String, List<Long>> request = Map.of("ids", List.of(testBoard.getId(), testBoard2.getId()));

            mockMvc.perform(delete("/api/boards")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andExpect(jsonPath("$.message").value("게시글 삭제 성공"))
                    .andDo(result -> log.debug("게시글 다중 삭제 응답: {}", result.getResponse().getContentAsString()));

            // 게시글과 관련 댓글이 삭제되었는지 확인
            assertFalse(boardService.getAllBoards().stream().anyMatch(b -> b.getId().equals(testBoard.getId())));
            assertFalse(boardService.getAllBoards().stream().anyMatch(b -> b.getId().equals(testBoard2.getId())));

            log.debug("게시글 다중 삭제 테스트 완료");
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("게시글 단일 삭제 성공")
        void testDeleteBoard() throws Exception {
            log.debug("게시글 단일 삭제 테스트 시작");

            // 댓글 추가 (cascade 삭제 테스트)
            CommentRequestDTO commentDTO = new CommentRequestDTO();
            commentDTO.setContent("Test Comment");
            commentService.addComment(testBoard.getId(), commentDTO, "testUser");

            mockMvc.perform(delete("/api/boards/" + testBoard.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andExpect(jsonPath("$.message").value("게시글 삭제 성공"))
                    .andDo(result -> log.debug("게시글 단일 삭제 응답: {}", result.getResponse().getContentAsString()));

            // 게시글과 관련 댓글이 삭제되었는지 확인
            assertFalse(boardService.getAllBoards().stream().anyMatch(b -> b.getId().equals(testBoard.getId())));

            log.debug("게시글 단일 삭제 테스트 완료");
        }

        @Test
        @WithMockUser(username = "testUser")
        @DisplayName("좋아요 추가 성공")
        void testAddLike() throws Exception {
            log.debug("좋아요 추가 테스트 시작");

            mockMvc.perform(post("/api/likes/" + testBoard.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andExpect(jsonPath("$.message").value("좋아요 추가 성공"))
                    .andDo(result -> log.debug("좋아요 추가 응답: {}", result.getResponse().getContentAsString()));

            log.debug("좋아요 추가 테스트 완료");
        }

        @Test
        @WithMockUser(username = "testUser")
        @DisplayName("댓글 추가 성공")
        void testAddComment() throws Exception {
            log.debug("댓글 추가 테스트 시작");

            CommentRequestDTO requestDTO = new CommentRequestDTO();
            requestDTO.setContent("Test Comment");

            mockMvc.perform(post("/api/comments/" + testBoard.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andExpect(jsonPath("$.message").value("댓글 추가 성공"))
                    .andDo(result -> log.debug("댓글 추가 응답: {}", result.getResponse().getContentAsString()));

            log.debug("댓글 추가 테스트 완료");
        }
    }
}
