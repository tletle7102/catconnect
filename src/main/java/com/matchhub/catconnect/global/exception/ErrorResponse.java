package com.matchhub.catconnect.global.exception;

import lombok.Getter;

@Getter
public class ErrorResponse {

    private final Domain domain;   // 에러가 발생한 도메인 (예: USER, BOARD 등)
    private final String code;     // 에러 코드 (예: "USER_001")
    private final String message;  // 에러 메시지 (예: "사용자를 찾을 수 없습니다.")
    private final int status;      // HTTP 상태 코드 (예: 404, 500 등)

    // 생성자: 에러 도메인, 에러 코드, 사용자 정의 메시지를 받아 ErrorResponse 객체 생성
    public ErrorResponse(Domain domain, ErrorCode errorCode, String message) {
        this.domain = domain;
        this.code = errorCode.getCode(); // 에러 코드 객체에서 코드 문자열 가져오기
        // 메시지가 null이 아니면 전달받은 메시지 사용, 아니면 기본 메시지 사용
        this.message = message != null ? message : errorCode.getMessage();
        this.status = errorCode.getHttpStatus().value(); // 상태 코드 숫자 추출
    }

    // 생성자 오버로딩: 메시지를 따로 전달하지 않을 때 this로 위 생성자를 호출하여 기본 메시지를 사용하도록 ErrorResponse 객체 생성
    public ErrorResponse(Domain domain, ErrorCode errorCode) {
        this(domain, errorCode, null);
    }
}
