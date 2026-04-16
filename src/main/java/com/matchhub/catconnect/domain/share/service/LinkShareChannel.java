package com.matchhub.catconnect.domain.share.service;

import com.matchhub.catconnect.domain.share.model.dto.ShareRequestDTO;
import com.matchhub.catconnect.domain.share.model.dto.ShareResponseDTO;
import com.matchhub.catconnect.domain.share.model.entity.ShareLink;
import com.matchhub.catconnect.domain.share.model.enums.ShareChannelType;
import com.matchhub.catconnect.domain.share.repository.ShareLinkRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

/**
 * 링크 공유 채널 구현체
 * 게시글의 단축 URL을 생성하여 반환함
 */
@Component
public class LinkShareChannel implements ShareChannel {

    private static final Logger log = LoggerFactory.getLogger(LinkShareChannel.class);

    // Base62 문자셋 (0-9, a-z, A-Z)
    private static final String BASE62_CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int SHORT_CODE_LENGTH = 8;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ShareLinkRepository shareLinkRepository;

    public LinkShareChannel(ShareLinkRepository shareLinkRepository) {
        this.shareLinkRepository = shareLinkRepository;
    }

    @Override
    public ShareChannelType getType() {
        return ShareChannelType.LINK;
    }

    @Override
    @Transactional
    public ShareResponseDTO share(ShareRequestDTO request) {
        log.debug("링크 공유 요청: boardId={}", request.getBoardId());

        Long boardId = request.getBoardId();
        String baseUrl = extractBaseUrl(request.getBoardUrl());

        // 기존 단축코드가 있는지 확인
        ShareLink shareLink = shareLinkRepository.findByBoardId(boardId)
                .orElseGet(() -> createShareLink(boardId));

        // 단축 URL 생성
        String shareUrl = baseUrl + "/s/" + shareLink.getShortCode();

        log.debug("단축 URL 생성 완료: shortCode={}, shareUrl={}", shareLink.getShortCode(), shareUrl);

        return ShareResponseDTO.success(
                ShareChannelType.LINK,
                "링크가 생성되었습니다.",
                shareUrl
        );
    }

    /**
     * 새로운 공유 링크 생성
     */
    private ShareLink createShareLink(Long boardId) {
        String shortCode = generateUniqueShortCode();
        ShareLink shareLink = new ShareLink(boardId, shortCode);
        return shareLinkRepository.save(shareLink);
    }

    /**
     * 중복되지 않는 단축코드 생성
     */
    private String generateUniqueShortCode() {
        String shortCode;
        int maxAttempts = 10;
        int attempts = 0;

        do {
            shortCode = generateShortCode();
            attempts++;
            if (attempts >= maxAttempts) {
                throw new RuntimeException("단축코드 생성에 실패했습니다. 최대 시도 횟수 초과");
            }
        } while (shareLinkRepository.findByShortCode(shortCode).isPresent());

        return shortCode;
    }

    /**
     * Base62 기반 랜덤 단축코드 생성
     */
    private String generateShortCode() {
        StringBuilder sb = new StringBuilder(SHORT_CODE_LENGTH);
        for (int i = 0; i < SHORT_CODE_LENGTH; i++) {
            int index = RANDOM.nextInt(BASE62_CHARS.length());
            sb.append(BASE62_CHARS.charAt(index));
        }
        return sb.toString();
    }

    /**
     * 전체 URL에서 기본 URL 추출 (도메인 + 포트)
     */
    private String extractBaseUrl(String fullUrl) {
        // http://localhost:8080/boards/1 → http://localhost:8080
        if (fullUrl == null) {
            return "";
        }
        int schemeEnd = fullUrl.indexOf("://");
        if (schemeEnd == -1) {
            return fullUrl;
        }
        int pathStart = fullUrl.indexOf("/", schemeEnd + 3);
        if (pathStart == -1) {
            return fullUrl;
        }
        return fullUrl.substring(0, pathStart);
    }
}
