package com.matchhub.catconnect.domain.board.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matchhub.catconnect.domain.board.model.dto.BoardRequestDTO;
import com.matchhub.catconnect.domain.board.model.dto.BoardResponseDTO;
import com.matchhub.catconnect.domain.board.service.BoardService;
import com.matchhub.catconnect.domain.comment.model.dto.CommentRequestDTO;
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

    private BoardResponseDTO testBoard; // 테스트용 게시글

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

        log.debug("테스트 설정 완료: boardId={}", testBoard.getId());
    }

    @AfterEach
    void tearDown() {
        log.debug("테스트 정리 시작");

        // 테스트용 게시글 삭제
        if (testBoard != null) {
            try {
                if (boardService.getBoardById(testBoard.getId()) != null) {
                    boardService.deleteBoard(testBoard.getId());
                    log.debug("테스트 정리 완료: boardId={}", testBoard.getId());
                }
            } catch (Exception e) {
                log.debug("테스트 정리: 이미 삭제된 게시글, boardId={}", testBoard.getId());
            }
        }

        // 남아있는 모든 게시글 정리
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
    @DisplayName("게시글 API 테스트") // 게시글 관련 API 테스트 그룹
    class BoardApiTests {

        @Test
        @DisplayName("게시글 목록 조회 성공")
        void testGetAllBoards() throws Exception {
            log.debug("게시글 목록 조회 테스트 시작");

            mockMvc.perform(get("/api/boards").contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk()) // HTTP 200 확인
                    .andExpect(jsonPath("$.result").value("SUCCESS")) // 성공 응답 확인
                    .andExpect(jsonPath("$.data[?(@.id == %d)].title", testBoard.getId()).value("Test Title")) // 생성한 게시글이 포함되었는지 확인
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
                    .andExpect(jsonPath("$.data.comments").isArray()) // 댓글 리스트 존재 여부 확인
                    .andDo(result -> log.debug("게시글 상세 조회 응답: {}", result.getResponse().getContentAsString()));

            log.debug("게시글 상세 조회 테스트 완료");
        }

        @Test
        @WithMockUser(username = "testUser") // Spring Security 인증 유저로 실행
        @DisplayName("게시글 생성 성공")
        void testCreateBoard() throws Exception {
            log.debug("게시글 생성 테스트 시작");

            // 새 게시글 생성 요청
            BoardRequestDTO requestDTO = new BoardRequestDTO();
            requestDTO.setTitle("New Title");
            requestDTO.setContent("New Content");

            mockMvc.perform(post("/api/boards")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isCreated()) // HTTP 201
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
                    .andDo(result -> log.debug("게시글 수정 응답: {}", result.getResponse().getContentAsString()));

            log.debug("게시글 수정 테스트 완료");
        }

        @Test
        @WithMockUser(roles = "ADMIN") // ADMIN 권한으로 삭제 테스트
        @DisplayName("게시글 삭제 성공")
        void testDeleteBoards() throws Exception {
            log.debug("게시글 삭제 테스트 시작");

            // 삭제 요청 페이로드 구성
            Map<String, List<Long>> request = Map.of("ids", List.of(testBoard.getId()));

            mockMvc.perform(delete("/api/boards")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andDo(result -> log.debug("게시글 삭제 응답: {}", result.getResponse().getContentAsString()));

            log.debug("게시글 삭제 테스트 완료");
        }

        @Test
        @WithMockUser(username = "testUser")
        @DisplayName("좋아요 추가 성공")
        void testAddLike() throws Exception {
            log.debug("좋아요 추가 테스트 시작");

            mockMvc.perform(post("/api/boards/" + testBoard.getId() + "/likes")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
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

            mockMvc.perform(post("/api/boards/" + testBoard.getId() + "/comments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andDo(result -> log.debug("댓글 추가 응답: {}", result.getResponse().getContentAsString()));

            log.debug("댓글 추가 테스트 완료");
        }
    }
}
