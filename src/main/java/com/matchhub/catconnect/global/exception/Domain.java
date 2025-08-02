package com.matchhub.catconnect.global.exception;

public enum Domain {
    USER("사용자"),
    BOARD("게시글"),
    COMMENT("댓글"),
    LIKE("좋아요");

    private final String description;

    Domain(String description) {
        this.description = description;
    }

    public String getDescription(){
        return description;
    }
}
