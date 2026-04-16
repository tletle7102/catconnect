package com.matchhub.catconnect.domain.share.service;

import com.matchhub.catconnect.domain.board.model.dto.BoardRequestDTO;
import com.matchhub.catconnect.domain.board.model.dto.BoardResponseDTO;
import com.matchhub.catconnect.domain.board.service.BoardService;
import com.matchhub.catconnect.domain.share.model.dto.ShareRequestDTO;
import com.matchhub.catconnect.domain.share.model.dto.ShareResponseDTO;
import com.matchhub.catconnect.domain.share.model.entity.ShareLink;
import com.matchhub.catconnect.domain.share.model.enums.ShareChannelType;
import com.matchhub.catconnect.domain.share.repository.ShareLinkRepository;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ShareService 통합 테스트
 */
@DisplayName("ShareService 테스트")
@SpringBootTest
class ShareServiceTest {

    private static final Logger log = LoggerFactory.getLogger(ShareServiceTest.class);

    @Autowired
    private ShareService shareService;

    @Autowired
    private ShareLinkRepository shareLinkRepository;

    @Autowired
    private BoardService boardService;

    @Autowired
    private List<ShareChannel> shareChannels;

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
    @DisplayName("채널 지원 테스트")
    class ChannelSupportTests {

        @Test
        @DisplayName("링크 채널 지원 확인")
        void testLinkChannelSupported() {
            log.debug("링크 채널 지원 확인 테스트 시작");

            assertTrue(shareService.isChannelSupported(ShareChannelType.LINK));

            log.debug("링크 채널 지원 확인 테스트 완료");
        }

        @Test
        @DisplayName("지원하는 채널 목록 조회")
        void testGetSupportedChannels() {
            log.debug("지원 채널 목록 조회 테스트 시작");

            List<ShareChannelType> channels = shareService.getSupportedChannels();

            assertNotNull(channels);
            assertTrue(channels.contains(ShareChannelType.LINK));

            log.debug("지원 채널 목록: {}", channels);
            log.debug("지원 채널 목록 조회 테스트 완료");
        }
    }

    @Nested
    @DisplayName("링크 공유 테스트")
    class LinkShareTests {

        @Test
        @DisplayName("링크 공유 성공 - 단축코드 생성")
        void testLinkShareSuccess() {
            log.debug("링크 공유 성공 테스트 시작");

            ShareRequestDTO request = new ShareRequestDTO();
            request.setBoardId(testBoard.getId());
            request.setBoardUrl("http://localhost:8080/boards/" + testBoard.getId());
            request.setChannelType(ShareChannelType.LINK);

            ShareResponseDTO response = shareService.share(request);

            assertNotNull(response);
            assertTrue(response.isSuccess());
            assertEquals(ShareChannelType.LINK, response.getChannelType());
            assertNotNull(response.getShareUrl());
            assertTrue(response.getShareUrl().contains("/s/"));

            log.debug("생성된 공유 URL: {}", response.getShareUrl());
            log.debug("링크 공유 성공 테스트 완료");
        }

        @Test
        @DisplayName("동일 게시글 재공유 시 기존 단축코드 반환")
        void testLinkShareSameBoard() {
            log.debug("동일 게시글 재공유 테스트 시작");

            ShareRequestDTO request = new ShareRequestDTO();
            request.setBoardId(testBoard.getId());
            request.setBoardUrl("http://localhost:8080/boards/" + testBoard.getId());
            request.setChannelType(ShareChannelType.LINK);

            // 첫 번째 공유
            ShareResponseDTO response1 = shareService.share(request);

            // 두 번째 공유
            ShareResponseDTO response2 = shareService.share(request);

            // 동일한 URL 반환 확인
            assertEquals(response1.getShareUrl(), response2.getShareUrl());

            // DB에 하나의 레코드만 존재 확인
            List<ShareLink> links = shareLinkRepository.findAll();
            assertEquals(1, links.size());

            log.debug("동일 게시글 재공유 테스트 완료");
        }

        @Test
        @DisplayName("단축코드가 8자리인지 확인")
        void testShortCodeLength() {
            log.debug("단축코드 길이 테스트 시작");

            ShareRequestDTO request = new ShareRequestDTO();
            request.setBoardId(testBoard.getId());
            request.setBoardUrl("http://localhost:8080/boards/" + testBoard.getId());
            request.setChannelType(ShareChannelType.LINK);

            shareService.share(request);

            Optional<ShareLink> shareLink = shareLinkRepository.findByBoardId(testBoard.getId());
            assertTrue(shareLink.isPresent());
            assertEquals(8, shareLink.get().getShortCode().length());

            log.debug("단축코드: {}", shareLink.get().getShortCode());
            log.debug("단축코드 길이 테스트 완료");
        }
    }

    @Nested
    @DisplayName("ShareChannel 구현체 테스트")
    class ShareChannelImplementationTests {

        @Test
        @DisplayName("모든 채널의 타입이 고유함")
        void testAllChannelsHaveUniqueTypes() {
            log.debug("채널 타입 고유성 테스트 시작");

            long distinctTypeCount = shareChannels.stream()
                    .map(ShareChannel::getType)
                    .distinct()
                    .count();

            assertEquals(shareChannels.size(), distinctTypeCount);

            log.debug("채널 타입 고유성 테스트 완료");
        }

        @Test
        @DisplayName("LinkShareChannel의 supports 메서드 동작 확인")
        void testLinkShareChannelSupports() {
            log.debug("LinkShareChannel supports 테스트 시작");

            ShareChannel linkChannel = shareChannels.stream()
                    .filter(c -> c.getType() == ShareChannelType.LINK)
                    .findFirst()
                    .orElseThrow();

            assertTrue(linkChannel.supports(ShareChannelType.LINK));
            assertFalse(linkChannel.supports(ShareChannelType.EMAIL));
            assertFalse(linkChannel.supports(ShareChannelType.KAKAO));

            log.debug("LinkShareChannel supports 테스트 완료");
        }
    }
}
