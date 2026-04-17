package com.matchhub.catconnect.domain.auth.controller;

import com.matchhub.catconnect.domain.auth.model.dto.LoginRequestDTO;
import com.matchhub.catconnect.domain.auth.model.dto.LoginResponseDTO;
import com.matchhub.catconnect.domain.auth.model.dto.TokenRefreshRequestDTO;
import com.matchhub.catconnect.domain.auth.model.dto.TokenRefreshResponseDTO;
import com.matchhub.catconnect.domain.auth.model.entity.RefreshToken;
import com.matchhub.catconnect.domain.auth.service.AuthService;
import com.matchhub.catconnect.domain.auth.service.RefreshTokenService;
import com.matchhub.catconnect.global.exception.AppException;
import com.matchhub.catconnect.global.exception.Domain;
import com.matchhub.catconnect.global.exception.ErrorCode;
import com.matchhub.catconnect.global.exception.Response;
import com.matchhub.catconnect.global.util.auth.JwtProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 인증 관련 REST API 컨트롤러
 *
 * Access Token + Refresh Token 기반 인증
 */
@Tag(name = "인증 API", description = "로그인/로그아웃/토큰재발급/인증상태확인 관련 REST API")
@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    private static final Logger log = LoggerFactory.getLogger(AuthRestController.class);
    private final AuthenticationManager authenticationManager;
    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final JwtProvider jwtProvider;

    public AuthRestController(AuthenticationManager authenticationManager,
                              AuthService authService,
                              RefreshTokenService refreshTokenService,
                              JwtProvider jwtProvider) {
        this.authenticationManager = authenticationManager;
        this.authService = authService;
        this.refreshTokenService = refreshTokenService;
        this.jwtProvider = jwtProvider;
    }

    @Operation(summary = "로그인", description = "사용자 인증 후 Access Token과 Refresh Token을 발급합니다")
    @PostMapping("/login")
    public ResponseEntity<Response<?>> login(
            @Parameter(description = "로그인 요청 정보")
            @RequestBody LoginRequestDTO loginRequest,
            HttpServletResponse response) {
        log.debug("POST /api/auth/login 요청: username={}", loginRequest.getUsername());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );
            log.debug("인증 성공: username={}", loginRequest.getUsername());

            String role = authentication.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");

            // AuthService를 통해 Access Token + Refresh Token 발급 및 쿠키 설정
            LoginResponseDTO responseDTO = authService.issueTokenAndSetCookie(
                    loginRequest.getUsername(), role, response, loginRequest.isStayLoggedIn());

            return ResponseEntity.ok(Response.success(responseDTO, "로그인 성공"));
        } catch (AuthenticationException e) {
            log.error("로그인 실패: username={}, error={}", loginRequest.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Response.error("사용자 이름 또는 비밀번호가 잘못되었습니다.", HttpStatus.UNAUTHORIZED));
        }
    }

    @Operation(summary = "토큰 재발급", description = "Refresh Token을 사용하여 새로운 Access Token을 발급합니다. Refresh Token Rotation이 적용되어 Refresh Token도 갱신됩니다.")
    @PostMapping("/refresh")
    public ResponseEntity<Response<?>> refreshToken(
            @Parameter(description = "Refresh Token (쿠키로 전달 시 생략 가능)")
            @RequestBody(required = false) TokenRefreshRequestDTO requestDTO,
            HttpServletRequest request,
            HttpServletResponse response) {
        log.debug("POST /api/auth/refresh 요청");

        try {
            // 1. Refresh Token 추출 (요청 바디 또는 쿠키에서)
            String refreshTokenValue = null;
            if (requestDTO != null && requestDTO.getRefreshToken() != null) {
                refreshTokenValue = requestDTO.getRefreshToken();
            } else {
                refreshTokenValue = authService.extractRefreshTokenFromCookie(request);
            }

            if (refreshTokenValue == null) {
                throw new AppException(Domain.AUTH, ErrorCode.REFRESH_TOKEN_NOT_FOUND,
                        "Refresh Token이 없습니다.");
            }

            // 2. Refresh Token 검증
            RefreshToken refreshToken = refreshTokenService.validateRefreshToken(refreshTokenValue);
            String username = refreshToken.getUser().getUsername();
            String role = refreshToken.getUser().getRole().name();

            // 3. 새 Access Token 발급
            String newAccessToken = authService.reissueAccessToken(username, role, response, false);

            // 4. Refresh Token Rotation (보안 강화)
            String newRefreshToken = refreshTokenService.rotateRefreshToken(refreshToken);
            authService.updateRefreshTokenCookie(newRefreshToken, response);

            log.debug("토큰 재발급 성공: username={}", username);

            // OAuth 2.0 스펙 준수: tokenType, expiresIn 포함
            long expiresIn = jwtProvider.getAccessTokenExpirationSeconds(false);
            TokenRefreshResponseDTO responseDTO = new TokenRefreshResponseDTO(
                    newAccessToken, newRefreshToken, "Bearer", expiresIn, username, role);
            return ResponseEntity.ok(Response.success(responseDTO, "토큰 재발급 성공"));

        } catch (AppException e) {
            log.error("토큰 재발급 실패: error={}", e.getMessage());
            return ResponseEntity.status(e.getErrorCode().getHttpStatus())
                    .body(Response.error(e.getMessage(), e.getErrorCode().getHttpStatus()));
        }
    }

    @Operation(summary = "로그아웃", description = "Access Token과 Refresh Token 쿠키를 삭제하고 DB에서 Refresh Token을 제거합니다")
    @PostMapping("/logout")
    public ResponseEntity<Response<Void>> logout(HttpServletRequest request, HttpServletResponse response) {
        log.debug("POST /api/auth/logout 요청");

        // AuthService를 통해 모든 인증 쿠키 삭제 및 DB에서 Refresh Token 제거
        authService.clearAllAuthCookies(request, response);

        log.debug("로그아웃 완료");
        return ResponseEntity.ok(Response.success(null, "로그아웃 성공"));
    }

    @Operation(summary = "인증 상태 확인", description = "현재 사용자의 인증 상태를 확인합니다")
    @GetMapping("/check")
    public ResponseEntity<Response<LoginResponseDTO>> checkAuthentication(HttpServletRequest request) {
        log.debug("GET /api/auth/check 요청");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            log.debug("인증 상태: 인증됨, username={}", authentication.getName());

            // 쿠키에서 JWT 토큰 추출
            String jwtToken = null;
            String refreshToken = null;
            String role = null;
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("jwtToken".equals(cookie.getName())) {
                        jwtToken = cookie.getValue();
                    }
                    if ("refreshToken".equals(cookie.getName())) {
                        refreshToken = cookie.getValue();
                    }
                }
            }

            // 권한 추출
            if (!authentication.getAuthorities().isEmpty()) {
                role = authentication.getAuthorities().iterator().next()
                        .getAuthority().replace("ROLE_", "");
            }

            LoginResponseDTO responseDTO = new LoginResponseDTO(
                    authentication.getName(),
                    role,
                    jwtToken,
                    refreshToken,
                    true
            );

            return ResponseEntity.ok(Response.success(responseDTO, "인증됨"));
        } else {
            log.debug("인증 상태: 비인증");
            LoginResponseDTO responseDTO = new LoginResponseDTO(null, null, null, null, false);
            return ResponseEntity.ok(Response.success(responseDTO, "비인증"));
        }
    }
}
