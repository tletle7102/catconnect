package com.matchhub.catconnect.global.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matchhub.catconnect.global.util.auth.JwtAuthenticationFilter;
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
 *
 * Access Token 만료와 일반 인증 실패를 구분하여 응답
 * 클라이언트는 TOKEN_EXPIRED 에러 코드를 받으면 /api/auth/refresh 호출하여 토큰 갱신
 */
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger log = LoggerFactory.getLogger(CustomAuthenticationEntryPoint.class);

    // JSON 응답을 만들기 위한 Jackson의 ObjectMapper
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {

        // 페이지 요청(text/html)인 경우 로그인 페이지로 리다이렉트
        String acceptHeader = request.getHeader("Accept");
        if (acceptHeader != null && acceptHeader.contains("text/html")) {
            String redirectUrl = request.getRequestURI();
            String queryString = request.getQueryString();
            if (queryString != null) {
                redirectUrl += "?" + queryString;
            }
            response.sendRedirect("/login?redirect=" + java.net.URLEncoder.encode(redirectUrl, "UTF-8"));
            return;
        }

        // 토큰 만료 여부 확인 (JwtAuthenticationFilter에서 설정한 attribute)
        Boolean tokenExpired = (Boolean) request.getAttribute(JwtAuthenticationFilter.TOKEN_EXPIRED_ATTRIBUTE);

        ErrorResponse errorResponse;

        if (Boolean.TRUE.equals(tokenExpired)) {
            // Access Token이 만료된 경우 - 클라이언트가 /api/auth/refresh 호출하도록 유도
            log.warn("Access Token 만료: requestURI={}", request.getRequestURI());
            errorResponse = new ErrorResponse(
                    Domain.AUTH,
                    ErrorCode.TOKEN_EXPIRED,
                    "Access Token이 만료되었습니다. Refresh Token으로 토큰을 갱신해주세요."
            );
        } else {
            // 일반 인증 실패 (토큰 없음, 토큰 형식 오류 등)
            log.error("인증 실패: requestURI={}, message={}", request.getRequestURI(), authException.getMessage());
            errorResponse = new ErrorResponse(
                    Domain.AUTH,
                    ErrorCode.AUTHENTICATION_FAILED,
                    "인증에 실패했습니다: " + authException.getMessage()
            );
        }

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
