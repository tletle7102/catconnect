package com.matchhub.catconnect.domain.auth.service;

import com.matchhub.catconnect.domain.auth.model.dto.LoginResponseDTO;
import com.matchhub.catconnect.global.util.auth.JwtProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 인증 관련 비즈니스 로직을 처리하는 서비스
 * OAuth 등 다양한 인증 방식 확장을 고려하여 설계됨
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final JwtProvider jwtProvider;

    public AuthService(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    /**
     * 사용자에게 JWT 토큰을 발급하고 쿠키에 설정
     * 회원가입 후 자동 로그인, OAuth 로그인 등 다양한 인증 방식에서 공통으로 사용
     *
     * @param username 사용자 이름
     * @param role     사용자 역할 (USER, ADMIN)
     * @param response HTTP 응답 객체
     * @return 로그인 응답 DTO
     */
    public LoginResponseDTO issueTokenAndSetCookie(String username, String role, HttpServletResponse response) {
        return issueTokenAndSetCookie(username, role, response, false);
    }

    /**
     * 사용자에게 JWT 토큰을 발급하고 쿠키에 설정 (로그인 상태 유지 옵션)
     *
     * @param username     사용자 이름
     * @param role         사용자 역할 (USER, ADMIN)
     * @param response     HTTP 응답 객체
     * @param stayLoggedIn 로그인 상태 유지 여부
     * @return 로그인 응답 DTO
     */
    public LoginResponseDTO issueTokenAndSetCookie(String username, String role, HttpServletResponse response, boolean stayLoggedIn) {
        log.debug("토큰 발급 시작: username={}, role={}, stayLoggedIn={}", username, role, stayLoggedIn);

        // JWT 토큰 생성
        String token = jwtProvider.generateToken(username, role, stayLoggedIn);

        // 쿠키 설정
        Cookie jwtCookie = createJwtCookie(token, stayLoggedIn);
        response.addCookie(jwtCookie);

        log.debug("토큰 발급 완료: username={}", username);

        return new LoginResponseDTO(username, role, token, true);
    }

    /**
     * JWT 쿠키 생성
     * 보안 설정을 일관되게 관리
     *
     * @param token        JWT 토큰
     * @param stayLoggedIn 로그인 상태 유지 여부
     * @return 설정된 쿠키
     */
    private Cookie createJwtCookie(String token, boolean stayLoggedIn) {
        Cookie jwtCookie = new Cookie("jwtToken", token);
        jwtCookie.setHttpOnly(false); // JavaScript에서 접근 필요
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge((int) jwtProvider.getExpirationSeconds(stayLoggedIn));
        jwtCookie.setSecure(false); // HTTPS 환경에서는 true로 변경 필요
        jwtCookie.setAttribute("SameSite", "Lax");
        return jwtCookie;
    }

    /**
     * JWT 쿠키 삭제 (로그아웃 시 사용)
     *
     * @param response HTTP 응답 객체
     */
    public void clearJwtCookie(HttpServletResponse response) {
        Cookie jwtCookie = new Cookie("jwtToken", null);
        jwtCookie.setHttpOnly(false);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0); // 즉시 만료
        jwtCookie.setSecure(false);
        response.addCookie(jwtCookie);
        log.debug("JWT 쿠키 삭제 완료");
    }
}
