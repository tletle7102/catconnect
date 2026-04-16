package com.matchhub.catconnect.domain.share.model.enums;

/**
 * 게시글 공유 채널 타입
 */
public enum ShareChannelType {
    LINK("링크 복사"),
    EMAIL("이메일"),
    KAKAO("카카오톡");

    private final String description;

    ShareChannelType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
