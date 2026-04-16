package com.matchhub.catconnect.domain.share.controller;

import com.matchhub.catconnect.domain.share.model.entity.ShareLink;
import com.matchhub.catconnect.domain.share.repository.ShareLinkRepository;
import com.matchhub.catconnect.global.exception.AppException;
import com.matchhub.catconnect.global.exception.Domain;
import com.matchhub.catconnect.global.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 단축 URL 리다이렉트 컨트롤러
 * /s/{shortCode} 요청을 /boards/{boardId}로 리다이렉트함
 */
@Controller
public class ShareRedirectController {

    private static final Logger log = LoggerFactory.getLogger(ShareRedirectController.class);

    private final ShareLinkRepository shareLinkRepository;

    public ShareRedirectController(ShareLinkRepository shareLinkRepository) {
        this.shareLinkRepository = shareLinkRepository;
    }

    @GetMapping("/s/{shortCode}")
    public String redirectToBoard(@PathVariable String shortCode) {
        log.debug("단축 URL 리다이렉트 요청: shortCode={}", shortCode);

        ShareLink shareLink = shareLinkRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new AppException(
                        Domain.BOARD,
                        ErrorCode.BOARD_NOT_FOUND,
                        "존재하지 않는 공유 링크입니다: " + shortCode
                ));

        log.debug("리다이렉트 대상: boardId={}", shareLink.getBoardId());

        return "redirect:/boards/" + shareLink.getBoardId();
    }
}
