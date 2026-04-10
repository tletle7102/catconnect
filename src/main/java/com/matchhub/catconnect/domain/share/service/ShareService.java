package com.matchhub.catconnect.domain.share.service;

import com.matchhub.catconnect.domain.share.model.dto.ShareRequestDTO;
import com.matchhub.catconnect.domain.share.model.dto.ShareResponseDTO;
import com.matchhub.catconnect.domain.share.model.enums.ShareChannelType;
import com.matchhub.catconnect.global.exception.AppException;
import com.matchhub.catconnect.global.exception.Domain;
import com.matchhub.catconnect.global.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 게시글 공유 통합 서비스
 * Strategy 패턴을 활용하여 다양한 공유 채널을 지원함
 */
@Service
public class ShareService {

    private static final Logger log = LoggerFactory.getLogger(ShareService.class);

    private final Map<ShareChannelType, ShareChannel> channels;

    public ShareService(List<ShareChannel> channelList) {
        this.channels = channelList.stream()
                .collect(Collectors.toMap(ShareChannel::getType, Function.identity()));
        log.debug("공유 서비스 초기화 완료: 등록된 채널={}", channels.keySet());
    }

    /**
     * 게시글 공유 실행
     * @param request 공유 요청 정보
     * @return 공유 결과
     */
    public ShareResponseDTO share(ShareRequestDTO request) {
        log.debug("공유 요청: channelType={}, boardId={}",
                request.getChannelType(), request.getBoardId());

        ShareChannel channel = getChannel(request.getChannelType());
        return channel.share(request);
    }

    /**
     * 특정 채널 지원 여부 확인
     */
    public boolean isChannelSupported(ShareChannelType type) {
        return channels.containsKey(type);
    }

    /**
     * 지원하는 모든 채널 목록 반환
     */
    public List<ShareChannelType> getSupportedChannels() {
        return List.copyOf(channels.keySet());
    }

    /**
     * 채널 조회
     */
    private ShareChannel getChannel(ShareChannelType type) {
        ShareChannel channel = channels.get(type);
        if (channel == null) {
            throw new AppException(
                    Domain.BOARD,
                    ErrorCode.INVALID_REQUEST,
                    "지원하지 않는 공유 채널입니다: " + type
            );
        }
        return channel;
    }
}
