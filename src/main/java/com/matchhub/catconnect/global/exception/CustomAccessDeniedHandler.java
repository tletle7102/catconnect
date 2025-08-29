package com.matchhub.catconnect.global.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 사용자가 인가(권한확인)되지 않은 리소스에 접근했을 때 실행되는 핸들러
 */
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private static final Logger log = LoggerFactory.getLogger(CustomAccessDeniedHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper(); // 객체를 JSON으로 변환해주는 Jackson 객체

    /**
     * 인가 실패 시 호출되는 메서드
     *
     * @param request 클라이언트의 HTTP 요청 정보
     * @param response 서버가 클라이언트에게 보낼 HTTP 응답 정보
     * @param accessDeniedException 인가(권한 없음) 예외 정보
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
        throws IOException, ServletException {

        // 로그로 인가 실패 정보 출력
        log.error("인가 실패: requestURI={}, message={}", request.getRequestURI(), accessDeniedException.getMessage());

        // 클라이언트에게 전달할 에러 응답 객체 생성
        ErrorResponse errorResponse = new ErrorResponse(
                Domain.AUTH,                            // 인증/인가 관련 도메인
                ErrorCode.ACCESS_DENIED,                // 권한 없음에 해당하는 에러 코드
                "접근 권한이 없습니다: " + accessDeniedException.getMessage()  // 상세 에러 메시지
        );

        // HTTP 응답 상태 코드 설정 (403 Forbidden)
        response.setStatus(HttpStatus.FORBIDDEN.value());

        // 응답 콘텐츠 타입 설정 (JSON)
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        // 문자 인코딩 설정 (UTF-8)
        response.setCharacterEncoding("UTF-8");

        // ErrorResponse 객체를 JSON 문자열로 변환하여 응답 본문에 작성
        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}
