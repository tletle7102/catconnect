package com.matchhub.catconnect.global.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@DisplayName("CustomAccessDeniedHandler 테스트") // 테스트 클래스의 설명 표시
class CustomAccessDeniedHandlerTest {

    private static final Logger log = LoggerFactory.getLogger(CustomAccessDeniedHandlerTest.class);
    private final CustomAccessDeniedHandler handler = new CustomAccessDeniedHandler(); // 테스트 대상 핸들러
    private HttpServletRequest request;
    private HttpServletResponse response;
    private AccessDeniedException accessDeniedException;
    private StringWriter stringWriter;
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("인가 실패 처리 테스트") // 인가 실패 케이스들을 모은 내부 클래스
    class AuthorizationFailureTests {

        @BeforeEach
        void setUp() throws IOException {
            log.debug("테스트 설정 시작");

            // HTTP 요청/응답 객체를 mock으로 생성
            request = mock(HttpServletRequest.class);
            response = mock(HttpServletResponse.class);

            // 인가 실패 예외 설정
            accessDeniedException = new AccessDeniedException("Access is denied");

            // 응답 본문을 문자열로 캡처하기 위한 writer 설정
            stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            objectMapper = new ObjectMapper();

            // mock 객체에 동작 정의
            when(request.getRequestURI()).thenReturn("/admin/boards");
            when(response.getWriter()).thenReturn(printWriter);

            log.debug("테스트 설정 완료");
        }

        @Test
        @DisplayName("인가 실패 시 ErrorResponse 반환 검증")
        void testHandleAuthorizationFailure() throws Exception {
            log.debug("인가 실패 처리 테스트 시작");

            // 핸들러 메서드 호출
            handler.handle(request, response,accessDeniedException);

            // HTTP 응답 관련 설정 검증
            verify(response).setStatus(HttpStatus.FORBIDDEN.value()); //403
            verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
            verify(response).setCharacterEncoding("UTF-8");

            // JSON 응답 문자열을 객체로 파싱하여 값 검증
            String jsonResponse = stringWriter.toString();
            ErrorResponse errorResponse = objectMapper.readValue(jsonResponse, ErrorResponse.class);

            // ErrorResponse 필드 값 검증
            assertEquals(Domain.AUTH, errorResponse.getDomain());                       // AUTH 도메인
            assertEquals("AUTH_002", errorResponse.getCode());                  // 인가 에러 코드
            assertTrue(errorResponse.getMessage().contains("접근 권한이 없습니다"));     // 기본 메시지 포함
            assertEquals(HttpStatus.FORBIDDEN.value(), errorResponse.getStatus());      // HTTP 403

            log.debug("인가 실패 처리 테스트 완료");
        }

        @Test
        @DisplayName("다른 인가 예외 메시지 처리 검증")
        void testHandleDifferentAccessDeniedException() throws Exception {
            log.debug("다른 인가 예외 테스트 시작");

            // 예외 메시지를 다르게 설정
            accessDeniedException = new AccessDeniedException("Insufficient permissions");

            // 핸들러 호출
            handler.handle(request, response, accessDeniedException);

            // 응답 메시지에 예외 메시지가 포함되어 있는지 확인
            String jsonResponse = stringWriter.toString();
            ErrorResponse errorResponse = objectMapper.readValue(jsonResponse, ErrorResponse.class);

            assertTrue(errorResponse.getMessage().contains("Insufficient permissions"));    // 메시지 확인
            assertEquals("AUTH_002", errorResponse.getCode());                  // 코드 확인

            log.debug("다른 인가 예외 테스트 완료");
        }
    }
}