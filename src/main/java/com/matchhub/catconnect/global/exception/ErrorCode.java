package com.matchhub.catconnect.global.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    // 사용자 관련 에러 (USER)
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_001", "사용자를 찾을 수 없습니다."),
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "USER_002", "이미 존재하는 사용자 이름 입니다."),
    USER_EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT,"USER_003", "이미 존재하는 이메일 입니다."),
    USER_INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "USER_004", "사용자 이름 또는 비밀번호가 잘못 되었습니다."),

    // 게시글 관련 에러 (BOARD)
    BOARD_NOT_FOUND(HttpStatus.NOT_FOUND, "BOARD_001", "게시글을 찾을 수 없습니다."),
    BOARD_UNAUTHORIZED(HttpStatus.FORBIDDEN, "BOARD_002", "게시글에 대한 권한이 없습니다."),

    // 댓글 관련 에러 (COMMENT)
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMENT_001", "댓글을 찾을 수 없습니다."),
    COMMENT_UNAUTHORIZED(HttpStatus.FORBIDDEN, "COMMENT_002", "댓글에 대한 권한이 없습니다."),

    // 좋아요 관련 에러 (LIKE)
    LIKE_ALREADY_EXISTS(HttpStatus.CONFLICT, "LIKE_001", "이미 좋아요를 눌렀습니다."),
    LIKE_NOT_FOUND(HttpStatus.NOT_FOUND, "LIKE_002", "좋아요를 찾을 수 없습니다."),

    // 파일 관련 에러 (FILE)
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "FILE_001", "파일을 찾을 수 없습니다."),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FILE_002", "파일 업로드에 실패했습니다."),
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "FILE_003", "허용되지 않는 파일 형식입니다."),
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST,  "FILE_004", "파일 크기가 제한을 초과했습니다."),

    // 인증/인가 관련 에러
    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "AUTH_001", "인증에 실패했습니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "AUTH_002", "접근 권한이 없습니다."),
    UNAUTHORIZED(HttpStatus.FORBIDDEN, "AUTH_003", "권한이 없습니다."),

    // 일반 에러
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "GLOBAL_001", "서버 내부 오류가 발생했습니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "GLOBAL_002", "잘못된 요청입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getCode(){
        return code;
    }

    public String getMessage(){
        return message;
    }
}
