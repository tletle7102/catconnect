package com.matchhub.catconnect.global.util.auth;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT 인증 필터
 *
 * 모든 요청에서 JWT 토큰을 추출하고 검증
 * Access Token 만료 시 request attribute에 만료 정보를 저장하여
 * CustomAuthenticationEntryPoint에서 적절한 에러 응답 반환 가능
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    // Request attribute 키: 토큰 만료 여부
    public static final String TOKEN_EXPIRED_ATTRIBUTE = "TOKEN_EXPIRED";

    private final JwtProvider jwtProvider;

    public JwtAuthenticationFilter(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        logger.debug("필터 처리 시작:  requestURI={}", request.getRequestURI());

        String token = resolveToken(request);
        if (token != null) {
            try {
                if (jwtProvider.validateToken(token)) {
                    String username = jwtProvider.getUsernameFromToken(token);
                    String role = jwtProvider.getRoleFromToken(token);
                    logger.debug("토큰 유효, 사용자 정보: username={}, role={}", username, role);
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            username, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                    );
                    logger.debug("인증 객체 생성: username={}, authorities={}", username, authentication.getAuthorities());

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.debug("SecurityContext 설정 완료: username={}", username);
                } else {
                    logger.warn("토큰 유효성 검증 실패: token={}", token.substring(0, Math.min(token.length(), 20)) + "...");
                    // 토큰이 만료된 경우 request attribute에 저장
                    if (jwtProvider.isTokenExpired(token)) {
                        request.setAttribute(TOKEN_EXPIRED_ATTRIBUTE, true);
                        logger.debug("토큰 만료됨, request attribute 설정: TOKEN_EXPIRED=true");
                    }
                }
            } catch (ExpiredJwtException e) {
                // 토큰 만료 예외 처리
                request.setAttribute(TOKEN_EXPIRED_ATTRIBUTE, true);
                logger.warn("토큰 만료: error={}", e.getMessage());
            } catch (Exception e) {
                logger.error("토큰 처리 중 예외 발생: error={}, token={}", e.getMessage(), token.substring(0, Math.min(token.length(), 20)) + "...");
            }
        } else {
            logger.debug("토큰 없음, 익명 사용자로 처리");
        }
        filterChain.doFilter(request, response);
        logger.debug("필터 체인 진행 완료: requestURI={}", request.getRequestURI());
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        logger.debug("Authorization 헤더 확인: header={}", bearerToken != null ? bearerToken.substring(0, Math.min(bearerToken.length(), 20)) + "..." : "없음");

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            logger.debug("Bearer 토큰 추출 (헤더): token={}", token.substring(0, Math.min(token.length(), 20)) + "...");
            return token;
        }

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwtToken".equals(cookie.getName())) {
                    String token = cookie.getValue();
                    logger.debug("Bearer 토큰 추출 (쿠키): token={}", token.substring(0, Math.min(token.length(), 20)) + "...");
                    return token;
                }
            }
        }
        logger.debug("쿠키에서 jwtToken 없음");

        logger.debug("Bearer 토큰 없음");
        return null;
    }
}
