package com.matchhub.catconnect.domain.like.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matchhub.catconnect.domain.board.model.dto.BoardRequestDTO;
import com.matchhub.catconnect.domain.board.model.dto.BoardResponseDTO;
import com.matchhub.catconnect.domain.board.service.BoardService;
import com.matchhub.catconnect.domain.like.model.dto.LikeResponseDTO;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * LikeRestController의 통합 테스트 클래스
 * 좋아요 관련 REST API 엔드포인트를 테스트하며, 실제 DB와 연동
 */
@DisplayName("LikeRestController 테스트")
@SpringBootTest
@AutoConfigureMockMvc
class LikeRestControllerTest {

    private static final Logger log = LoggerFactory.getLogger(LikeRestControllerTest.class);

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
    @DisplayName("좋아요 API 테스트")
    class LikeApiTests {

        @Test
        @WithMockUser(username = "testUser")
        @DisplayName("전체 좋아요 조회 성공")
        void testGetAllLikes() throws Exception {
            log.debug("전체 좋아요 조회 테스트 시작");

            // 좋아요 추가
            mockMvc.perform(post("/api/likes/" + testBoard.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            mockMvc.perform(get("/api/likes")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.content[0].boardId").value(testBoard.getId()))
                    .andExpect(jsonPath("$.data.content[0].username").value("testUser"))
                    .andDo(result -> log.debug("전체 좋아요 조회 응답: {}", result.getResponse().getContentAsString()));

            log.debug("전체 좋아요 조회 테스트 완료");
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
        @DisplayName("중복 좋아요 추가 실패")
        void testAddDuplicateLike() throws Exception {
            log.debug("중복 좋아요 추가 테스트 시작");

            // 첫 번째 좋아요 추가
            mockMvc.perform(post("/api/likes/" + testBoard.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            // 두 번째 좋아요 시도
            mockMvc.perform(post("/api/likes/" + testBoard.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.domain").value("LIKE"))
                    .andExpect(jsonPath("$.code").value("LIKE_001"))
                    .andExpect(jsonPath("$.message").value("이미 좋아요를 눌렀습니다."))
                    .andDo(result -> log.debug("중복 좋아요 추가 응답: {}", result.getResponse().getContentAsString()));

            log.debug("중복 좋아요 추가 테스트 완료");
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("좋아요 삭제 성공")
        void testDeleteLike() throws Exception {
            log.debug("좋아요 삭제 테스트 시작");

            // 좋아요 추가
            mockMvc.perform(post("/api/likes/" + testBoard.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .with(user("testUser").roles("USER")))
                    .andExpect(status().isOk());

            // 좋아요 ID 조회
            BoardResponseDTO board = boardService.getBoardById(testBoard.getId());
            assertFalse(board.getLikes().isEmpty(), "좋아요 리스트가 비어 있습니다.");
            LikeResponseDTO like = board.getLikes().stream()
                    .filter(l -> l.getUsername().equals("testUser"))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("testUser의 좋아요가 없습니다."));
            assertNotNull(like.getId(), "좋아요 ID가 null입니다.");
            Long likeId = like.getId();

            mockMvc.perform(delete("/api/likes/" + likeId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andExpect(jsonPath("$.message").value("좋아요 삭제 성공"))
                    .andDo(result -> log.debug("좋아요 삭제 응답: {}", result.getResponse().getContentAsString()));

            log.debug("좋아요 삭제 테스트 완료");
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("다중 좋아요 삭제 성공")
        void testDeleteLikes() throws Exception {
            log.debug("다중 좋아요 삭제 테스트 시작");

            // 좋아요 추가
            mockMvc.perform(post("/api/likes/" + testBoard.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .with(user("testUser").roles("USER")))
                    .andExpect(status().isOk());

            // 좋아요 ID 조회
            BoardResponseDTO board = boardService.getBoardById(testBoard.getId());
            assertFalse(board.getLikes().isEmpty(), "좋아요 리스트가 비어 있습니다.");
            LikeResponseDTO like = board.getLikes().stream()
                    .filter(l -> l.getUsername().equals("testUser"))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("testUser의 좋아요가 없습니다."));
            assertNotNull(like.getId(), "좋아요 ID가 null입니다.");
            Long likeId = like.getId();

            // 삭제 요청 페이로드
            Map<String, List<Long>> request = Map.of("ids", List.of(likeId));

            mockMvc.perform(delete("/api/likes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andExpect(jsonPath("$.message").value("좋아요 삭제 성공"))
                    .andDo(result -> log.debug("다중 좋아요 삭제 응답: {}", result.getResponse().getContentAsString()));

            log.debug("다중 좋아요 삭제 테스트 완료");
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("다수 좋아요 중 특정 사용자 좋아요 삭제 성공")
        void testDeleteLikeWithMultipleLikes() throws Exception {
            log.debug("다수 좋아요 삭제 테스트 시작");

            // 여러 사용자 좋아요 추가
            String[] users = {"testUser", "user2", "user3"};
            for (String username : users) {
                mockMvc.perform(post("/api/likes/" + testBoard.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(user(username).roles("USER")))
                        .andExpect(status().isOk());
            }

            // 좋아요 ID 조회
            BoardResponseDTO board = boardService.getBoardById(testBoard.getId());
            assertEquals(3, board.getLikes().size(), "좋아요 개수가 예상과 다릅니다.");
            LikeResponseDTO like = board.getLikes().stream()
                    .filter(l -> l.getUsername().equals("testUser"))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("testUser의 좋아요가 없습니다."));
            assertNotNull(like.getId(), "좋아요 ID가 null입니다.");
            Long likeId = like.getId();

            mockMvc.perform(delete("/api/likes/" + likeId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andExpect(jsonPath("$.message").value("좋아요 삭제 성공"))
                    .andDo(result -> log.debug("다수 좋아요 삭제 응답: {}", result.getResponse().getContentAsString()));

            // 삭제 확인
            board = boardService.getBoardById(testBoard.getId());
            assertEquals(2, board.getLikes().size(), "좋아요 삭제 후 개수가 예상과 다릅니다.");

            log.debug("다수 좋아요 삭제 테스트 완료");
        }
    }
}
