package com.matchhub.catconnect.global.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 인증 실패 시 호출되는 EntryPoint 구현체 (401 Unauthorized 응답 처리용)
 */
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger log = LoggerFactory.getLogger(CustomAuthenticationEntryPoint.class);

    // JSON 응답을 만들기 위한 Jackson의 ObjectMapper
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {

        // 인증 예외가 발생했을 때 로그 출력 (요청 URI와 예외 메시지 포함)
        log.error("인증 실패: requestURI={}, message={}", request.getRequestURI(), authException.getMessage());

        // 에러 응답 객체 생성 (에러 도메인: AUTH, 에러 코드: AUTHENTICATION_FAILED, 메시지 포함)
        ErrorResponse errorResponse = new ErrorResponse(
                Domain.AUTH,
                ErrorCode.AUTHENTICATION_FAILED,
                "인증에 실패했습니다: " + authException.getMessage()
        );

        // HTTP 상태 코드 401 (Unauthorized) 설정
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        // 응답 본문 타입을 JSON으로 설정
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        // 한글 인코딩을 위해 UTF-8 지정
        response.setCharacterEncoding("UTF-8");

        // ErrorResponse 객체를 JSON으로 변환하여 응답 본문에 작성
        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}
