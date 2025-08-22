package com.matchhub.catconnect.global.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// 전역적으로 발생하는 예외를 처리하는 클래스임을 선언 (RestController 대상)
@RestControllerAdvice
public class CustomExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(CustomExceptionHandler.class);

    /**
     * 개발자가 정의한 AppException 처리 핸들러
     * - AppException 발생 시 ErrorResponse 객체를 만들어 응답으로 반환
     * - HTTP 상태 코드는 ErrorCode에 정의된 값 사용
     */
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> handleAppException(AppException ex) {
        // 로그에 예외 정보 출력 (어느 도메인에서 어떤 에러 코드가 발생했는지)
        log.error("AppException 발생: domain={}, code={}, message={}",
                ex.getDomain(), ex.getErrorCode().getCode(), ex.getMessage());

        // 예외 정보 기반으로 ErrorResponse 객체 생성
        ErrorResponse errorResponse = new ErrorResponse(ex.getDomain(), ex.getErrorCode(), ex.getMessage());

        // ErrorCode에 정의된 HttpStatus를 사용하여 응답 생성
        return ResponseEntity.status(ex.getErrorCode().getHttpStatus()).body(errorResponse);
    }

    /**
     * AppException 이외의 모든 예외 처리 핸들러
     * - 예상하지 못한 일반적인 예외를 처리
     * - INTERNAL_SERVER_ERROR(500) 상태 코드로 응답
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        // 예외 로그 출력 (스택트레이스 포함)
        log.error("예상치 못한 예외 발생: message={}", ex.getMessage(), ex);

        // 공통 에러 응답 객체 생성 (도메인 없음, 내부 서버 오류)
        ErrorResponse errorResponse = new ErrorResponse(Domain.NONE, ErrorCode.INTERNAL_SERVER_ERROR);

        // 500 에러 응답 반환
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
