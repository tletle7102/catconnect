package com.matchhub.catconnect.domain.board.model.enums;

public enum BoardCategory {
    NOTICE("공지사항", "운영", 1),
    GREETING("가입인사", "운영", 2),
    CAT_SHOW("고양이 자랑", "소통", 3),
    FREE("자유게시판", "소통", 4),
    STRAY_CAT("길고양이 이야기", "소통", 5),
    QNA("질문과 답변", "정보", 6),
    HEALTH("건강·의료 정보", "정보", 7),
    REVIEW("용품 후기", "정보", 8),
    RESCUE("임시보호·구조", "나눔", 9),
    FREE_SHARE("무료나눔", "나눔", 10);

    private final String displayName;
    private final String group;
    private final int sortOrder;

    BoardCategory(String displayName, String group, int sortOrder) {
        this.displayName = displayName;
        this.group = group;
        this.sortOrder = sortOrder;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getGroup() {
        return group;
    }

    public int getSortOrder() {
        return sortOrder;
    }
}
