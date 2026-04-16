package com.matchhub.catconnect.domain.share.controller;

import com.matchhub.catconnect.domain.board.model.dto.BoardResponseDTO;
import com.matchhub.catconnect.domain.board.service.BoardService;
import com.matchhub.catconnect.domain.share.model.dto.ShareRequestDTO;
import com.matchhub.catconnect.domain.share.model.dto.ShareResponseDTO;
import com.matchhub.catconnect.domain.share.model.enums.ShareChannelType;
import com.matchhub.catconnect.domain.share.service.ShareService;
import com.matchhub.catconnect.global.exception.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 게시글 공유 REST API 컨트롤러
 */
@RestController
@RequestMapping("/api/shares")
@Tag(name = "공유 API", description = "게시글 공유 관련 API")
public class ShareRestController {

    private static final Logger log = LoggerFactory.getLogger(ShareRestController.class);

    private final ShareService shareService;
    private final BoardService boardService;

    public ShareRestController(ShareService shareService, BoardService boardService) {
        this.shareService = shareService;
        this.boardService = boardService;
    }

    @GetMapping("/boards/{boardId}/link")
    @Operation(summary = "게시글 공유 링크 조회", description = "게시글의 공유 링크를 반환합니다.")
    public ResponseEntity<Response<ShareResponseDTO>> getShareLink(
            @PathVariable Long boardId,
            HttpServletRequest request) {

        log.debug("공유 링크 조회 요청: boardId={}", boardId);

        // 게시글 존재 여부 확인
        BoardResponseDTO board = boardService.getBoardById(boardId);

        // 공유 URL 생성
        String baseUrl = getBaseUrl(request);
        String boardUrl = baseUrl + "/boards/" + boardId;

        ShareRequestDTO shareRequest = new ShareRequestDTO();
        shareRequest.setBoardId(boardId);
        shareRequest.setBoardTitle(board.getTitle());
        shareRequest.setBoardUrl(boardUrl);
        shareRequest.setChannelType(ShareChannelType.LINK);

        ShareResponseDTO response = shareService.share(shareRequest);

        return ResponseEntity.ok(Response.success(response));
    }

    /**
     * 요청에서 기본 URL 추출
     */
    private String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();

        if ((scheme.equals("http") && serverPort == 80) ||
                (scheme.equals("https") && serverPort == 443)) {
            return scheme + "://" + serverName;
        }
        return scheme + "://" + serverName + ":" + serverPort;
    }
}