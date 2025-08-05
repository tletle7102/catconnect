package com.matchhub.catconnect.global.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

class ErrorCodeTest {
    // 특정 ErrorCode 상수의 속성값들이 정확한지 테스트
    @Test
    void testErrorCodeProperties() {
        ErrorCode errorCode = ErrorCode.USER_NOT_FOUND;

        // Http 상태 코드가 404(NOT_FOUND)인지 확인
        assertEquals(HttpStatus.NOT_FOUND, errorCode.getHttpStatus());

        // 에러 코드 문자열이 "USER_001"인지 확인
        assertEquals("USER_001", errorCode.getCode());

        // 에러 메시지가 "사용자를 찾을 수 없습니다."인지 확인
        assertEquals("사용자를 찾을 수 없습니다.", errorCode.getMessage());
    }

    // ErrorCode enum에 정의된 값들이 올바른지 전반적으로 테스트
    @Test
    void testErrorCodeValues() {
        // 특정 에러 코드의 문자열 값 확인
        assertEquals("BOARD_001", ErrorCode.BOARD_NOT_FOUND.getCode());
        assertEquals("AUTH_001", ErrorCode.AUTHENTICATION_FAILED.getCode());

        // 내부 서버 오류의 Http 상태 코드가 500인지 확인
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus());
    }

    // 문자열로 enum 상수를 가져오고, 잘못된 입력에 대한 예외 처리 확인
    @Test
    void testErrorCodeEnum() {
        // 문자열로부터 USER_NOT_FOUND enum 상수를 정확히 가져오는지 확인
        assertEquals(ErrorCode.USER_NOT_FOUND, ErrorCode.valueOf("USER_NOT_FOUND"));

        // 존재하지 않는 코드로 enum을 요청할 경우 예외가 발생하는지 확인
        assertThrows(IllegalArgumentException.class, () -> ErrorCode.valueOf("INVALID_CODE"));
    }
}
