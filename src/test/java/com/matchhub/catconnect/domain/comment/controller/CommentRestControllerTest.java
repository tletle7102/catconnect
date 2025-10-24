package com.matchhub.catconnect.domain.comment.controller;

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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * CommentRestController의 통합 테스트 클래스
 * 댓글 관련 REST API 엔드포인트를 테스트하며, 실제 DB와 연동
 */
@DisplayName("CommentRestController 테스트")
@SpringBootTest
@AutoConfigureMockMvc
class CommentRestControllerTest {

    private static final Logger log = LoggerFactory.getLogger(CommentRestControllerTest.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BoardService boardService;

    private BoardResponseDTO testBoard;

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
    @DisplayName("댓글 API 테스트")
    class CommentApiTests {

        @Test
        @WithMockUser(username = "testUser")
        @DisplayName("전체 댓글 조회 성공")
        void testGetAllComments() throws Exception {
            log.debug("전체 댓글 조회 테스트 시작");

            // 댓글 추가
            CommentRequestDTO requestDTO = new CommentRequestDTO();
            requestDTO.setContent("Test Comment");
            mockMvc.perform(post("/api/comments/" + testBoard.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDTO)));

            mockMvc.perform(get("/api/comments")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andExpect(jsonPath("$.data[0].content").value("Test Comment"))
                    .andDo(result -> log.debug("전체 댓글 조회 응답: {}", result.getResponse().getContentAsString()));

            log.debug("전체 댓글 조회 테스트 완료");
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

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("댓글 삭제 성공")
        void testDeleteComment() throws Exception {
            log.debug("댓글 삭제 테스트 시작");

            // 댓글 추가
            CommentRequestDTO requestDTO = new CommentRequestDTO();
            requestDTO.setContent("Test Comment");
            mockMvc.perform(post("/api/comments/" + testBoard.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDTO))
                    .with(user("testUser").roles("USER")));

            // 댓글 ID 조회
            BoardResponseDTO board = boardService.getBoardById(testBoard.getId());
            Long commentId = board.getComments().get(0).getId();

            mockMvc.perform(delete("/api/comments/" + commentId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andExpect(jsonPath("$.message").value("댓글 삭제 성공"))
                    .andDo(result -> log.debug("댓글 삭제 응답: {}", result.getResponse().getContentAsString()));

            log.debug("댓글 삭제 테스트 완료");
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("다중 댓글 삭제 성공")
        void testDeleteComments() throws Exception {
            log.debug("다중 댓글 삭제 테스트 시작");

            // 댓글 추가
            CommentRequestDTO requestDTO = new CommentRequestDTO();
            requestDTO.setContent("Test Comment");
            mockMvc.perform(post("/api/comments/" + testBoard.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDTO))
                    .with(user("testUser").roles("USER")));

            // 댓글 ID 조회
            BoardResponseDTO board = boardService.getBoardById(testBoard.getId());
            Long commentId = board.getComments().get(0).getId();

            // 삭제 요청 페이로드
            Map<String, List<Long>> request = Map.of("ids", List.of(commentId));

            mockMvc.perform(delete("/api/comments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andExpect(jsonPath("$.message").value("댓글 삭제 성공"))
                    .andDo(result -> log.debug("다중 댓글 삭제 응답: {}", result.getResponse().getContentAsString()));

            log.debug("다중 댓글 삭제 테스트 완료");
        }
    }
}
