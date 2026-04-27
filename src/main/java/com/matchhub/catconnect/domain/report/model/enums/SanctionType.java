package com.matchhub.catconnect.domain.report.model.enums;

public enum SanctionType {
    WARNING,              // 1단계: 경고
    POST_BAN_TEMP,        // 2단계: 7일 작성 정지
    POST_BAN_PERMANENT,   // 3단계: 영구 작성 정지
    ACCOUNT_BAN           // 4단계: 강제 탈퇴
}
