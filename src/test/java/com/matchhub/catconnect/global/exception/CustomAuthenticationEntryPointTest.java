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
import org.springframework.security.core.AuthenticationException;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@DisplayName("CustomAuthenticationEntryPoint 테스트")
class CustomAuthenticationEntryPointTest {

    private static final Logger log = LoggerFactory.getLogger(CustomAuthenticationEntryPointTest.class);

    // 테스트 대상 객체
    private final CustomAuthenticationEntryPoint entryPoint = new CustomAuthenticationEntryPoint();

    // 목(mock) 객체 및 유틸 필드들
    private HttpServletRequest request;
    private HttpServletResponse response;
    private AuthenticationException authException;
    private StringWriter stringWriter; // 응답 본문을 문자열로 받기 위한 writer
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("인증 실패 처리 테스트")
    class AuthenticationFailureTests {

        @BeforeEach
        void setUp() throws IOException {
            log.debug("테스트 설정 시작");
            // 요청과 응답을 목(mock)으로 생성
            request = mock(HttpServletRequest.class);
            response = mock(HttpServletResponse.class);

            // 인증 예외를 임의로 생성
            authException = new AuthenticationException("Invalid token") {};

            // 응답을 문자열로 받아올 수 있도록 설정
            stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            objectMapper = new ObjectMapper();

            // 요청 URI와 응답 Writer에 대한 동작 설정
            when(request.getRequestURI()).thenReturn("/boards/new");
            when(response.getWriter()).thenReturn(printWriter);
            log.debug("테스트 설정 완료");
        }

        @Test
        @DisplayName("인증 실패 시 ErrorResponse 반환 검증")
        void testCommenceAuthenticationFailure() throws Exception {
            log.debug("인증 실패 처리 테스트 시작");

            // 실제 entryPoint 호출
            entryPoint.commence(request, response, authException);

            // HTTP 상태 코드 및 헤더 값 검증
            verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
            verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
            verify(response).setCharacterEncoding("UTF-8");

            // 응답 본문을 문자열로 읽어 JSON 객체로 파싱
            String jsonResponse = stringWriter.toString();
            ErrorResponse errorResponse = objectMapper.readValue(jsonResponse, ErrorResponse.class);

            // ErrorResponse의 각 필드 값 검증
            assertEquals(Domain.AUTH, errorResponse.getDomain());
            assertEquals("AUTH_001", errorResponse.getCode());
            assertTrue(errorResponse.getMessage().contains("인증에 실패했습니다"));
            assertEquals(HttpStatus.UNAUTHORIZED.value(), errorResponse.getStatus());

            log.debug("인증 실패 처리 테스트 완료");
        }

        @Test
        @DisplayName("다른 인증 예외 메시지 처리 검증")
        void testCommenceDifferentAuthException() throws Exception {
            log.debug("다른 인증 예외 테스트 시작");

            // 예외 메시지가 다른 인증 예외 시나리오 설정
            authException = new AuthenticationException("No credentials provided") {};

            // 테스트 수행
            entryPoint.commence(request, response, authException);

            // JSON 응답 파싱 후 메시지 및 코드 확인
            String jsonResponse = stringWriter.toString();
            ErrorResponse errorResponse = objectMapper.readValue(jsonResponse, ErrorResponse.class);

            assertTrue(errorResponse.getMessage().contains("No credentials provided"));
            assertEquals("AUTH_001", errorResponse.getCode());

            log.debug("다른 인증 예외 테스트 완료");
        }
    }
}
