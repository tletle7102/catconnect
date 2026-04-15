package com.matchhub.catconnect.domain.auth.service;

import com.matchhub.catconnect.domain.auth.model.entity.RefreshToken;
import com.matchhub.catconnect.domain.auth.repository.RefreshTokenRepository;
import com.matchhub.catconnect.domain.user.model.entity.User;
import com.matchhub.catconnect.domain.user.repository.UserRepository;
import com.matchhub.catconnect.global.exception.AppException;
import com.matchhub.catconnect.global.exception.Domain;
import com.matchhub.catconnect.global.exception.ErrorCode;
import com.matchhub.catconnect.global.util.auth.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Refresh Token 관리 서비스
 *
 * 책임:
 * - Refresh Token 생성 및 DB 저장
 * - 토큰 조회 및 유효성 검증
 * - 토큰 재발급 시 기존 토큰 갱신 (Rotation)
 * - 로그아웃 시 토큰 삭제
 *
 * 추후 Redis 전환 시 이 서비스의 구현만 변경하면 됨 (Strategy 패턴 적용 가능)
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RefreshTokenService {

    private static final Logger logger = LoggerFactory.getLogger(RefreshTokenService.class);

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    /**
     * Refresh Token 생성 및 DB 저장
     *
     * @param user 사용자 엔티티
     * @return 생성된 Refresh Token 문자열
     */
    @Transactional
    public String createRefreshToken(User user) {
        String tokenValue = jwtProvider.generateRefreshToken(user.getUsername());
        LocalDateTime expiredAt = jwtProvider.getRefreshTokenExpiredAt();

        RefreshToken refreshToken = new RefreshToken(user, tokenValue, expiredAt);
        refreshTokenRepository.save(refreshToken);

        logger.info("Refresh Token 생성: userId={}, username={}", user.getId(), user.getUsername());
        return tokenValue;
    }

    /**
     * Refresh Token 생성 및 DB 저장 (username으로 조회)
     *
     * @param username 사용자명
     * @return 생성된 Refresh Token 문자열
     */
    @Transactional
    public String createRefreshToken(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(Domain.USER, ErrorCode.USER_NOT_FOUND,
                        "사용자를 찾을 수 없습니다: " + username));
        return createRefreshToken(user);
    }

    /**
     * Refresh Token 검증 및 사용자 정보 반환
     *
     * 검증 항목:
     * 1. JWT 형식 및 서명 유효성
     * 2. DB에 토큰 존재 여부
     * 3. 토큰 만료 여부
     *
     * @param tokenValue Refresh Token 문자열
     * @return RefreshToken 엔티티
     */
    public RefreshToken validateRefreshToken(String tokenValue) {
        // 1. JWT 형식 검증
        if (!jwtProvider.validateToken(tokenValue)) {
            logger.warn("유효하지 않은 Refresh Token 형식");
            throw new AppException(Domain.AUTH, ErrorCode.REFRESH_TOKEN_INVALID,
                    "유효하지 않은 Refresh Token입니다.");
        }

        // 2. DB에서 토큰 조회
        RefreshToken refreshToken = refreshTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> {
                    logger.warn("DB에서 Refresh Token을 찾을 수 없음");
                    return new AppException(Domain.AUTH, ErrorCode.REFRESH_TOKEN_NOT_FOUND,
                            "Refresh Token을 찾을 수 없습니다.");
                });

        // 3. 만료 여부 확인
        if (refreshToken.isExpired()) {
            logger.warn("Refresh Token 만료: userId={}", refreshToken.getUser().getId());
            // 만료된 토큰은 삭제
            refreshTokenRepository.delete(refreshToken);
            throw new AppException(Domain.AUTH, ErrorCode.REFRESH_TOKEN_EXPIRED,
                    "Refresh Token이 만료되었습니다.");
        }

        return refreshToken;
    }

    /**
     * Refresh Token Rotation - 기존 토큰 갱신
     *
     * 보안 강화를 위해 Refresh Token 사용 시 새 토큰으로 교체
     *
     * @param oldRefreshToken 기존 RefreshToken 엔티티
     * @return 새로운 Refresh Token 문자열
     */
    @Transactional
    public String rotateRefreshToken(RefreshToken oldRefreshToken) {
        String newTokenValue = jwtProvider.generateRefreshToken(oldRefreshToken.getUser().getUsername());
        LocalDateTime newExpiredAt = jwtProvider.getRefreshTokenExpiredAt();

        oldRefreshToken.updateToken(newTokenValue, newExpiredAt);
        refreshTokenRepository.save(oldRefreshToken);

        logger.info("Refresh Token Rotation: userId={}", oldRefreshToken.getUser().getId());
        return newTokenValue;
    }

    /**
     * 특정 Refresh Token 삭제 (단일 기기 로그아웃)
     *
     * @param tokenValue 삭제할 Refresh Token 문자열
     */
    @Transactional
    public void deleteRefreshToken(String tokenValue) {
        refreshTokenRepository.deleteByToken(tokenValue);
        logger.info("Refresh Token 삭제 (단일)");
    }

    /**
     * 사용자의 모든 Refresh Token 삭제 (전체 기기 로그아웃)
     *
     * @param username 사용자명
     */
    @Transactional
    public void deleteAllRefreshTokensByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(Domain.USER, ErrorCode.USER_NOT_FOUND,
                        "사용자를 찾을 수 없습니다: " + username));

        refreshTokenRepository.deleteByUser(user);
        logger.info("Refresh Token 전체 삭제: username={}", username);
    }

    /**
     * 사용자 ID로 모든 Refresh Token 삭제
     *
     * @param userId 사용자 ID
     */
    @Transactional
    public void deleteAllRefreshTokensByUserId(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
        logger.info("Refresh Token 전체 삭제: userId={}", userId);
    }

    /**
     * 만료된 Refresh Token 일괄 정리 (스케줄러에서 호출)
     *
     * @return 삭제된 토큰 수 (로깅용)
     */
    @Transactional
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
        logger.info("만료된 Refresh Token 정리 완료");
    }
}
