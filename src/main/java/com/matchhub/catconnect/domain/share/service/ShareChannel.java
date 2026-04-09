package com.matchhub.catconnect.domain.share.service;

import com.matchhub.catconnect.domain.share.model.dto.ShareRequestDTO;
import com.matchhub.catconnect.domain.share.model.dto.ShareResponseDTO;
import com.matchhub.catconnect.domain.share.model.enums.ShareChannelType;

/**
 * 게시글 공유 채널 인터페이스 (Strategy 패턴)
 * 각 공유 방식별 구현체가 이 인터페이스를 구현함
 */
public interface ShareChannel {

    /**
     * 이 채널이 지원하는 공유 타입 반환
     */
    ShareChannelType getType();

    /**
     * 해당 타입을 지원하는지 확인
     */
    default boolean supports(ShareChannelType type) {
        return getType() == type;
    }

    /**
     * 공유 실행
     * @param request 공유 요청 정보
     * @return 공유 결과
     */
    ShareResponseDTO share(ShareRequestDTO request);
}

