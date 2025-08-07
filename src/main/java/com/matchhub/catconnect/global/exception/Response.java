package com.matchhub.catconnect.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class Response<T> {
    private final String result; // 요청 처리 결과 (SUCCESS 또는 ERROR)
    private final T data;        // 응답 데이터 (성공 시 포함)
    private final String message; // 응답 메시지
    private final int status;    // HTTP 상태 코드 (숫자)

    // 성공 응답을 위한 생성자
    public Response(T data, String message, HttpStatus status) {
        this.result = "SUCCESS";
        this.data = data;
        this.message = message;
        this.status = status.value();
    }

    // 에러 응답을 위한 생성자: 비즈니스 로직 에러같은 단순 에러 상황을 컨트롤러에서 직접 처리
    public Response(String message, HttpStatus status) {
        this.result = "ERROR";
        this.data = null;
        this.message = message;
        this.status = status.value();
    }

    // 성공 응답을 간편하게 생성하는 정적 메서드 (커스텀 메시지 사용)
    public static <T> Response<T> success(T data, String message) {
        return new Response<>(data, message, HttpStatus.OK);
    }

    // 성공 응답을 간편하게 생성하는 정적 메서드 (기본 메시지 사용)
    public static <T> Response<T> success(T data) {
        return new Response<>(data, "요청이 성공적으로 처리되었습니다.", HttpStatus.OK);
    }

    // 에러 응답을 간편하게 생성하는 정적 메서드
    public static Response<?> error(String message, HttpStatus status) {
        return new Response<>(message, status);
    }
}
