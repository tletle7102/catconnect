package com.matchhub.catconnect.domain.share.controller;

import com.matchhub.catconnect.domain.board.model.dto.BoardRequestDTO;
import com.matchhub.catconnect.domain.board.model.dto.BoardResponseDTO;
import com.matchhub.catconnect.domain.board.service.BoardService;
import com.matchhub.catconnect.domain.share.repository.ShareLinkRepository;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ShareRestController 통합 테스트
 */
@DisplayName("ShareRestController 테스트")
@SpringBootTest
@AutoConfigureMockMvc
class ShareRestControllerTest {

    private static final Logger log = LoggerFactory.getLogger(ShareRestControllerTest.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BoardService boardService;

    @Autowired
    private ShareLinkRepository shareLinkRepository;

    private BoardResponseDTO testBoard;

    @BeforeEach
    void setUp() {
        log.debug("테스트 설정 시작");

        // 기존 공유 링크 삭제
        shareLinkRepository.deleteAll();

        // 기존 게시글 삭제
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

        // 공유 링크 삭제
        shareLinkRepository.deleteAll();

        // 게시글 삭제
        List<Long> ids = boardService.getAllBoards().stream().map(BoardResponseDTO::getId).toList();
        if (!ids.isEmpty()) {
            try {
                boardService.deleteBoards(ids);
            } catch (Exception e) {
                log.debug("데이터 정리 실패: {}", e.getMessage());
            }
        }

        log.debug("테스트 정리 완료");
    }

    @Nested
    @DisplayName("공유 링크 조회 API 테스트")
    class GetShareLinkTests {

        @Test
        @DisplayName("공유 링크 조회 성공")
        void testGetShareLinkSuccess() throws Exception {
            log.debug("공유 링크 조회 성공 테스트 시작");

            mockMvc.perform(get("/api/shares/boards/" + testBoard.getId() + "/link")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.channelType").value("LINK"))
                    .andExpect(jsonPath("$.data.success").value(true))
                    .andExpect(jsonPath("$.data.shareUrl").exists())
                    .andDo(result -> log.debug("공유 링크 조회 응답: {}", result.getResponse().getContentAsString()));

            log.debug("공유 링크 조회 성공 테스트 완료");
        }

        @Test
        @DisplayName("존재하지 않는 게시글 공유 링크 조회 실패")
        void testGetShareLinkNotFound() throws Exception {
            log.debug("존재하지 않는 게시글 공유 링크 조회 테스트 시작");

            mockMvc.perform(get("/api/shares/boards/99999/link")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("BOARD_001"))
                    .andDo(result -> log.debug("에러 응답: {}", result.getResponse().getContentAsString()));

            log.debug("존재하지 않는 게시글 공유 링크 조회 테스트 완료");
        }

        @Test
        @DisplayName("동일 게시글 재조회 시 동일한 단축코드 반환")
        void testGetShareLinkSameCode() throws Exception {
            log.debug("동일 단축코드 반환 테스트 시작");

            // 첫 번째 조회
            String response1 = mockMvc.perform(get("/api/shares/boards/" + testBoard.getId() + "/link")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            // 두 번째 조회
            String response2 = mockMvc.perform(get("/api/shares/boards/" + testBoard.getId() + "/link")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            // 응답이 동일한지 확인
            assertEquals(response1, response2);

            log.debug("동일 단축코드 반환 테스트 완료");
        }
    }

    @Nested
    @DisplayName("단축 URL 리다이렉트 테스트")
    class RedirectTests {

        @Test
        @DisplayName("단축 URL 리다이렉트 성공")
        void testRedirectSuccess() throws Exception {
            log.debug("단축 URL 리다이렉트 테스트 시작");

            // 먼저 공유 링크 생성
            mockMvc.perform(get("/api/shares/boards/" + testBoard.getId() + "/link")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            // 생성된 단축코드 조회
            String shortCode = shareLinkRepository.findByBoardId(testBoard.getId())
                    .orElseThrow()
                    .getShortCode();

            // 리다이렉트 테스트
            mockMvc.perform(get("/s/" + shortCode))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/boards/" + testBoard.getId()))
                    .andDo(result -> log.debug("리다이렉트 응답: {}", result.getResponse().getRedirectedUrl()));

            log.debug("단축 URL 리다이렉트 테스트 완료");
        }

        @Test
        @DisplayName("존재하지 않는 단축코드 리다이렉트 실패")
        void testRedirectNotFound() throws Exception {
            log.debug("존재하지 않는 단축코드 리다이렉트 테스트 시작");

            mockMvc.perform(get("/s/notexist"))
                    .andExpect(status().isNotFound())
                    .andDo(result -> log.debug("에러 응답: {}", result.getResponse().getContentAsString()));

            log.debug("존재하지 않는 단축코드 리다이렉트 테스트 완료");
        }
    }
}
