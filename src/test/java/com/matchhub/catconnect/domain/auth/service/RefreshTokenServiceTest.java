package com.matchhub.catconnect.domain.auth.service;

import com.matchhub.catconnect.domain.auth.model.entity.RefreshToken;
import com.matchhub.catconnect.domain.auth.repository.RefreshTokenRepository;
import com.matchhub.catconnect.domain.user.model.entity.User;
import com.matchhub.catconnect.domain.user.model.enums.Role;
import com.matchhub.catconnect.domain.user.repository.UserRepository;
import com.matchhub.catconnect.global.exception.AppException;
import com.matchhub.catconnect.global.exception.ErrorCode;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RefreshTokenService 통합 테스트
 */
@DisplayName("RefreshTokenService 테스트")
@SpringBootTest
@Transactional
class RefreshTokenServiceTest {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenServiceTest.class);

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;

    @BeforeEach
    void setUp() {
        log.debug("테스트 설정 시작");

        // 기존 데이터 정리
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();

        // 테스트 사용자 생성
        testUser = new User(
                "testuser",
                "test@gmail.com",
                passwordEncoder.encode("password123"),
                Role.USER
        );
        userRepository.save(testUser);

        log.debug("테스트 설정 완료: userId={}", testUser.getId());
    }

    @AfterEach
    void tearDown() {
        log.debug("테스트 정리 시작");
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
        log.debug("테스트 정리 완료");
    }

    @Nested
    @DisplayName("Refresh Token 생성 테스트")
    class CreateRefreshTokenTests {

        @Test
        @DisplayName("User 엔티티로 Refresh Token 생성 성공")
        void testCreateRefreshTokenWithUser() {
            // when
            String tokenValue = refreshTokenService.createRefreshToken(testUser);

            // then
            assertNotNull(tokenValue);
            assertTrue(tokenValue.length() > 0);

            Optional<RefreshToken> savedToken = refreshTokenRepository.findByToken(tokenValue);
            assertTrue(savedToken.isPresent());
            assertEquals(testUser.getId(), savedToken.get().getUser().getId());
            assertFalse(savedToken.get().isExpired());

            log.debug("Refresh Token 생성 성공: tokenLength={}", tokenValue.length());
        }

        @Test
        @DisplayName("username으로 Refresh Token 생성 성공")
        void testCreateRefreshTokenWithUsername() {
            // when
            String tokenValue = refreshTokenService.createRefreshToken("testuser");

            // then
            assertNotNull(tokenValue);

            Optional<RefreshToken> savedToken = refreshTokenRepository.findByToken(tokenValue);
            assertTrue(savedToken.isPresent());

            log.debug("username으로 Refresh Token 생성 성공");
        }

        @Test
        @DisplayName("존재하지 않는 username으로 생성 시 실패")
        void testCreateRefreshTokenWithInvalidUsername() {
            // when & then
            AppException exception = assertThrows(AppException.class, () ->
                    refreshTokenService.createRefreshToken("nonexistent")
            );

            assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
            log.debug("존재하지 않는 username 예외 발생: {}", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Refresh Token 검증 테스트")
    class ValidateRefreshTokenTests {

        @Test
        @DisplayName("유효한 Refresh Token 검증 성공")
        void testValidateRefreshTokenSuccess() {
            // given
            String tokenValue = refreshTokenService.createRefreshToken(testUser);

            // when
            RefreshToken refreshToken = refreshTokenService.validateRefreshToken(tokenValue);

            // then
            assertNotNull(refreshToken);
            assertEquals(testUser.getId(), refreshToken.getUser().getId());
            assertFalse(refreshToken.isExpired());

            log.debug("Refresh Token 검증 성공");
        }

        @Test
        @DisplayName("DB에 없는 Refresh Token 검증 실패")
        void testValidateRefreshTokenNotFound() {
            // given - 유효한 JWT 형식이지만 DB에 저장되지 않은 토큰
            String tokenValue = refreshTokenService.createRefreshToken(testUser);
            refreshTokenRepository.deleteAll(); // DB에서 삭제

            // when & then
            AppException exception = assertThrows(AppException.class, () ->
                    refreshTokenService.validateRefreshToken(tokenValue)
            );

            assertEquals(ErrorCode.REFRESH_TOKEN_NOT_FOUND, exception.getErrorCode());
            log.debug("DB에 없는 토큰 검증 실패: {}", exception.getMessage());
        }

        @Test
        @DisplayName("유효하지 않은 형식의 Refresh Token 검증 실패")
        void testValidateRefreshTokenInvalidFormat() {
            // when & then
            AppException exception = assertThrows(AppException.class, () ->
                    refreshTokenService.validateRefreshToken("invalid.token.format")
            );

            assertEquals(ErrorCode.REFRESH_TOKEN_INVALID, exception.getErrorCode());
            log.debug("유효하지 않은 형식 검증 실패: {}", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Refresh Token Rotation 테스트")
    class RotateRefreshTokenTests {

        @Test
        @DisplayName("Refresh Token Rotation 성공 - 새로운 토큰 값 반환")
        void testRotateRefreshToken() {
            // given
            String oldTokenValue = refreshTokenService.createRefreshToken(testUser);
            RefreshToken oldRefreshToken = refreshTokenService.validateRefreshToken(oldTokenValue);

            // when
            String newTokenValue = refreshTokenService.rotateRefreshToken(oldRefreshToken);

            // then - 새 토큰이 생성되고, 기존 토큰과 다름을 검증
            assertNotNull(newTokenValue);
            assertNotEquals(oldTokenValue, newTokenValue);
            assertTrue(newTokenValue.length() > 0);

            // 새 토큰도 유효한 JWT 형식인지 확인 (점 2개로 분리)
            assertEquals(3, newTokenValue.split("\\.").length);

            log.debug("Refresh Token Rotation 성공: oldToken != newToken");
        }
    }

    @Nested
    @DisplayName("Refresh Token 삭제 테스트")
    class DeleteRefreshTokenTests {

        @Test
        @DisplayName("특정 Refresh Token 삭제 성공")
        void testDeleteRefreshToken() {
            // given
            String tokenValue = refreshTokenService.createRefreshToken(testUser);
            assertTrue(refreshTokenRepository.findByToken(tokenValue).isPresent());

            // when
            refreshTokenService.deleteRefreshToken(tokenValue);

            // then
            assertFalse(refreshTokenRepository.findByToken(tokenValue).isPresent());
            log.debug("특정 Refresh Token 삭제 성공");
        }

        @Test
        @DisplayName("사용자의 모든 Refresh Token 삭제 성공")
        void testDeleteAllRefreshTokensByUsername() {
            // given - 여러 개의 Refresh Token 생성
            refreshTokenService.createRefreshToken(testUser);
            refreshTokenService.createRefreshToken(testUser);
            refreshTokenService.createRefreshToken(testUser);

            long countBefore = refreshTokenRepository.count();
            assertTrue(countBefore >= 3);

            // when
            refreshTokenService.deleteAllRefreshTokensByUsername("testuser");

            // then
            long countAfter = refreshTokenRepository.count();
            assertEquals(0, countAfter);

            log.debug("모든 Refresh Token 삭제 성공: before={}, after={}", countBefore, countAfter);
        }
    }
}
