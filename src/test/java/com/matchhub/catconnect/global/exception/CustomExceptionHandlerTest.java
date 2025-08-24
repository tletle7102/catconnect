package com.matchhub.catconnect.global.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CustomExceptionHandler 테스트")
class CustomExceptionHandlerTest {

    private static final Logger log = LoggerFactory.getLogger(CustomExceptionHandlerTest.class);

    // 테스트 대상인 예외 핸들러 인스턴스 생성
    private final CustomExceptionHandler exceptionHandler = new CustomExceptionHandler();

    @Nested
    @DisplayName("AppException 처리 테스트")
    class AppExceptionTests {

        @Test
        @DisplayName("AppException 기본 처리 검증")
        void testHandleAppException() {
            log.debug("AppException 기본 처리 테스트 시작");

            // USER_NOT_FOUND 예외 객체 생성 (기본 메시지 사용)
            AppException ex = new AppException(Domain.USER, ErrorCode.USER_NOT_FOUND);

            // 예외 처리 핸들러 메서드 호출
            ResponseEntity<ErrorResponse> response = exceptionHandler.handleAppException(ex);

            // 응답 상태 코드와 에러 정보가 예상대로 나오는지 확인
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertEquals(Domain.USER, response.getBody().getDomain());
            assertEquals("USER_001", response.getBody().getCode());
            assertEquals("사용자를 찾을 수 없습니다.", response.getBody().getMessage());

            log.debug("AppException 기본 처리 테스트 완료");
        }

        @Test
        @DisplayName("AppException 커스텀 메시지 처리 검증")
        void testHandleAppExceptionCustomMessage() {
            log.debug("AppException 커스텀 메시지 테스트 시작");

            // 커스텀 메시지를 포함한 예외 객체 생성
            AppException ex = new AppException(Domain.BOARD, ErrorCode.BOARD_NOT_FOUND, "게시글 ID: 999 없음");

            // 예외 처리 핸들러 호출
            ResponseEntity<ErrorResponse> response = exceptionHandler.handleAppException(ex);

            // 응답이 커스텀 메시지를 정확히 반영하는지 확인
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertEquals(Domain.BOARD, response.getBody().getDomain());
            assertEquals("BOARD_001", response.getBody().getCode());
            assertEquals("게시글 ID: 999 없음", response.getBody().getMessage());

            log.debug("AppException 커스텀 메시지 테스트 완료");
        }
    }

    @Nested
    @DisplayName("일반 예외 처리 테스트")
    class GeneralExceptionTests {

        @Test
        @DisplayName("일반 예외 처리 검증")
        void testHandleGeneralException() {
            log.debug("일반 예외 테스트 시작");

            // 예상치 못한 일반 예외(RuntimeException) 생성
            Exception ex = new RuntimeException("예상치 못한 오류");

            // 예외 처리 핸들러 호출
            ResponseEntity<ErrorResponse> response = exceptionHandler.handleGeneralException(ex);

            // INTERNAL_SERVER_ERROR 상태와 기본 메시지가 반환되는지 확인
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            assertEquals(Domain.NONE, response.getBody().getDomain());
            assertEquals("GLOBAL_001", response.getBody().getCode());
            assertEquals("서버 내부 오류가 발생했습니다.", response.getBody().getMessage());

            log.debug("일반 예외 테스트 완료");
        }

        @Test
        @DisplayName("다른 일반 예외 메시지 검증")
        void testGeneralExceptionDifferentMessage() {
            log.debug("일반 예외 메시지 테스트 시작");

            // 또 다른 일반 예외(RuntimeException) 생성
            Exception ex = new RuntimeException("DB 오류");

            // 처리 결과 검증 (실제 메시지는 무시되고 고정된 메시지가 반환됨)
            ResponseEntity<ErrorResponse> response = exceptionHandler.handleGeneralException(ex);

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            assertEquals(Domain.NONE, response.getBody().getDomain());
            assertEquals("GLOBAL_001", response.getBody().getCode());

            log.debug("일반 예외 메시지 테스트 완료");
        }
    }
}