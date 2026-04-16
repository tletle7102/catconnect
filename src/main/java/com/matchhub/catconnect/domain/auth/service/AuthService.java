package com.matchhub.catconnect.domain.auth.service;

import com.matchhub.catconnect.domain.auth.model.dto.LoginResponseDTO;
import com.matchhub.catconnect.domain.user.model.entity.User;
import com.matchhub.catconnect.domain.user.repository.UserRepository;
import com.matchhub.catconnect.global.exception.AppException;
import com.matchhub.catconnect.global.exception.Domain;
import com.matchhub.catconnect.global.exception.ErrorCode;
import com.matchhub.catconnect.global.util.auth.JwtProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인증 관련 비즈니스 로직을 처리하는 서비스
 *
 * Access Token + Refresh Token 발급 및 관리
 * OAuth 등 다양한 인증 방식 확장을 고려하여 설계됨
 */
@Service
public class AuthService {

	private static final Logger log = LoggerFactory.getLogger(AuthService.class);

	private static final String ACCESS_TOKEN_COOKIE_NAME = "jwtToken";
	private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

	private final JwtProvider jwtProvider;
	private final RefreshTokenService refreshTokenService;
	private final UserRepository userRepository;

	public AuthService(JwtProvider jwtProvider, RefreshTokenService refreshTokenService, UserRepository userRepository) {
		this.jwtProvider = jwtProvider;
		this.refreshTokenService = refreshTokenService;
		this.userRepository = userRepository;
	}

	/**
	 * 사용자에게 Access Token + Refresh Token을 발급하고 쿠키에 설정
	 * 회원가입 후 자동 로그인, OAuth 로그인 등 다양한 인증 방식에서 공통으로 사용
	 *
	 * @param username 사용자 이름
	 * @param role     사용자 역할 (USER, ADMIN)
	 * @param response HTTP 응답 객체
	 * @return 로그인 응답 DTO
	 */
	@Transactional
	public LoginResponseDTO issueTokenAndSetCookie(String username, String role, HttpServletResponse response) {
		return issueTokenAndSetCookie(username, role, response, false);
	}

	/**
	 * 사용자에게 Access Token + Refresh Token을 발급하고 쿠키에 설정 (로그인 상태 유지 옵션)
	 *
	 * @param username     사용자 이름
	 * @param role         사용자 역할 (USER, ADMIN)
	 * @param response     HTTP 응답 객체
	 * @param stayLoggedIn 로그인 상태 유지 여부
	 * @return 로그인 응답 DTO
	 */
	@Transactional
	public LoginResponseDTO issueTokenAndSetCookie(String username, String role, HttpServletResponse response, boolean stayLoggedIn) {
		log.debug("토큰 발급 시작: username={}, role={}, stayLoggedIn={}", username, role, stayLoggedIn);

		// Access Token 생성
		String accessToken = jwtProvider.generateAccessToken(username, role, stayLoggedIn);

		// Refresh Token 생성 및 DB 저장
		String refreshToken = refreshTokenService.createRefreshToken(username);

		// Access Token 쿠키 설정
		Cookie accessTokenCookie = createAccessTokenCookie(accessToken, stayLoggedIn);
		response.addCookie(accessTokenCookie);

		// Refresh Token 쿠키 설정
		Cookie refreshTokenCookie = createRefreshTokenCookie(refreshToken);
		response.addCookie(refreshTokenCookie);

		log.debug("토큰 발급 완료: username={}", username);

		// OAuth 2.0 스펙 준수: tokenType, expiresIn 포함
		long expiresIn = jwtProvider.getAccessTokenExpirationSeconds(stayLoggedIn);
		return new LoginResponseDTO(username, role, accessToken, refreshToken, "Bearer", expiresIn, true);
	}

	/**
	 * Access Token만 재발급 (Refresh Token으로 갱신 시 사용)
	 *
	 * @param username     사용자 이름
	 * @param role         사용자 역할
	 * @param response     HTTP 응답 객체
	 * @param stayLoggedIn 로그인 상태 유지 여부
	 * @return 새로운 Access Token
	 */
	public String reissueAccessToken(String username, String role, HttpServletResponse response, boolean stayLoggedIn) {
		String accessToken = jwtProvider.generateAccessToken(username, role, stayLoggedIn);
		Cookie accessTokenCookie = createAccessTokenCookie(accessToken, stayLoggedIn);
		response.addCookie(accessTokenCookie);
		return accessToken;
	}

	/**
	 * Refresh Token 쿠키 갱신 (Rotation 시 사용)
	 *
	 * @param refreshToken 새로운 Refresh Token
	 * @param response     HTTP 응답 객체
	 */
	public void updateRefreshTokenCookie(String refreshToken, HttpServletResponse response) {
		Cookie refreshTokenCookie = createRefreshTokenCookie(refreshToken);
		response.addCookie(refreshTokenCookie);
	}

	/**
	 * Access Token 쿠키 생성
	 *
	 * @param token        Access Token
	 * @param stayLoggedIn 로그인 상태 유지 여부
	 * @return 설정된 쿠키
	 */
	private Cookie createAccessTokenCookie(String token, boolean stayLoggedIn) {
		Cookie cookie = new Cookie(ACCESS_TOKEN_COOKIE_NAME, token);
		cookie.setHttpOnly(false); // JavaScript에서 접근 필요
		cookie.setPath("/");
		cookie.setMaxAge((int) jwtProvider.getAccessTokenExpirationSeconds(stayLoggedIn));
		cookie.setSecure(false); // HTTPS 환경에서는 true로 변경 필요
		cookie.setAttribute("SameSite", "Lax");
		return cookie;
	}

	/**
	 * Refresh Token 쿠키 생성
	 *
	 * @param token Refresh Token
	 * @return 설정된 쿠키
	 */
	private Cookie createRefreshTokenCookie(String token) {
		Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, token);
		cookie.setHttpOnly(true); // Refresh Token은 JavaScript 접근 차단 (보안 강화)
		cookie.setPath("/");
		cookie.setMaxAge((int) jwtProvider.getRefreshTokenExpirationSeconds());
		cookie.setSecure(false); // HTTPS 환경에서는 true로 변경 필요
		cookie.setAttribute("SameSite", "Strict"); // CSRF 방지를 위해 Strict
		return cookie;
	}

	/**
	 * 모든 인증 쿠키 삭제 (로그아웃 시 사용)
	 *
	 * @param request  HTTP 요청 객체
	 * @param response HTTP 응답 객체
	 */
	@Transactional
	public void clearAllAuthCookies(HttpServletRequest request, HttpServletResponse response) {
		// Refresh Token DB에서 삭제
		String refreshToken = extractRefreshTokenFromCookie(request);
		if (refreshToken != null) {
			refreshTokenService.deleteRefreshToken(refreshToken);
		}

		// Access Token 쿠키 삭제
		Cookie accessTokenCookie = new Cookie(ACCESS_TOKEN_COOKIE_NAME, null);
		accessTokenCookie.setHttpOnly(false);
		accessTokenCookie.setPath("/");
		accessTokenCookie.setMaxAge(0);
		accessTokenCookie.setSecure(false);
		response.addCookie(accessTokenCookie);

		// Refresh Token 쿠키 삭제
		Cookie refreshTokenCookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, null);
		refreshTokenCookie.setHttpOnly(true);
		refreshTokenCookie.setPath("/");
		refreshTokenCookie.setMaxAge(0);
		refreshTokenCookie.setSecure(false);
		response.addCookie(refreshTokenCookie);

		log.debug("모든 인증 쿠키 삭제 완료");
	}

	/**
	 * 기존 메서드 유지 (하위 호환성)
	 */
	public void clearJwtCookie(HttpServletResponse response) {
		Cookie accessTokenCookie = new Cookie(ACCESS_TOKEN_COOKIE_NAME, null);
		accessTokenCookie.setHttpOnly(false);
		accessTokenCookie.setPath("/");
		accessTokenCookie.setMaxAge(0);
		accessTokenCookie.setSecure(false);
		response.addCookie(accessTokenCookie);

		Cookie refreshTokenCookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, null);
		refreshTokenCookie.setHttpOnly(true);
		refreshTokenCookie.setPath("/");
		refreshTokenCookie.setMaxAge(0);
		refreshTokenCookie.setSecure(false);
		response.addCookie(refreshTokenCookie);

		log.debug("JWT 쿠키 삭제 완료");
	}

	/**
	 * 요청에서 Refresh Token 쿠키 추출
	 */
	public String extractRefreshTokenFromCookie(HttpServletRequest request) {
		if (request.getCookies() != null) {
			for (Cookie cookie : request.getCookies()) {
				if (REFRESH_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
					return cookie.getValue();
				}
			}
		}
		return null;
	}

	/**
	 * username으로 User 조회 후 role 반환
	 */
	public String getRoleByUsername(String username) {
		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new AppException(Domain.USER, ErrorCode.USER_NOT_FOUND,
						"사용자를 찾을 수 없습니다: " + username));
		return user.getRole().name();
	}
}
