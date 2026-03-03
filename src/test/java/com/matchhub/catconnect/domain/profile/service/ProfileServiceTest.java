package com.matchhub.catconnect.domain.profile.service;

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

import static org.junit.jupiter.api.Assertions.*;

/**
 * ProfileService 통합 테스트
 */
@DisplayName("ProfileService 테스트")
@SpringBootTest
class ProfileServiceTest {

    private static final Logger log = LoggerFactory.getLogger(ProfileServiceTest.class);

    @Autowired
    private ProfileService profileService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;

    @BeforeEach
    void setUp() {
        log.debug("테스트 설정 시작");

        userRepository.deleteAll();

        testUser = new User("testuser", "test@gmail.com", passwordEncoder.encode("password123"), Role.USER);
        userRepository.save(testUser);

        log.debug("테스트 설정 완료: userId={}", testUser.getId());
    }

    @AfterEach
    void tearDown() {
        log.debug("테스트 정리 시작");
        userRepository.deleteAll();
        log.debug("테스트 정리 완료");
    }

    @Nested
    @DisplayName("프로필 이미지 URL 조회 테스트")
    class GetProfileImageUrlTests {

        @Test
        @DisplayName("프로필 이미지가 없는 경우 null 반환")
        void testGetProfileImageUrlWhenNoImage() {
            log.debug("프로필 이미지 없음 테스트 시작");

            String profileImageUrl = profileService.getProfileImageUrl("testuser");

            assertNull(profileImageUrl);

            log.debug("프로필 이미지 없음 테스트 완료");
        }

        @Test
        @DisplayName("프로필 이미지가 있는 경우 URL 반환")
        void testGetProfileImageUrlWhenImageExists() {
            log.debug("프로필 이미지 있음 테스트 시작");

            // 프로필 이미지 URL 직접 설정
            testUser.updateProfileImage("/api/files/download/test-image.jpg");
            userRepository.save(testUser);

            String profileImageUrl = profileService.getProfileImageUrl("testuser");

            assertEquals("/api/files/download/test-image.jpg", profileImageUrl);

            log.debug("프로필 이미지 있음 테스트 완료");
        }

        @Test
        @DisplayName("존재하지 않는 사용자 조회 시 예외 발생")
        void testGetProfileImageUrlUserNotFound() {
            log.debug("사용자 없음 예외 테스트 시작");

            AppException exception = assertThrows(AppException.class, () ->
                    profileService.getProfileImageUrl("nonexistent")
            );
            assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());

            log.debug("사용자 없음 예외 테스트 완료");
        }
    }

    @Nested
    @DisplayName("프로필 이미지 삭제 테스트")
    class DeleteProfileImageTests {

        @Test
        @DisplayName("프로필 이미지 삭제 성공")
        void testDeleteProfileImage() {
            log.debug("프로필 이미지 삭제 테스트 시작");

            // 프로필 이미지 설정
            testUser.updateProfileImage("/api/files/download/test-image.jpg");
            userRepository.save(testUser);

            // 삭제
            profileService.deleteProfileImage("testuser");

            // 확인
            User updatedUser = userRepository.findByUsername("testuser").orElseThrow();
            assertNull(updatedUser.getProfileImageUrl());

            log.debug("프로필 이미지 삭제 테스트 완료");
        }

        @Test
        @DisplayName("존재하지 않는 사용자의 프로필 이미지 삭제 시 예외 발생")
        void testDeleteProfileImageUserNotFound() {
            log.debug("사용자 없음 삭제 예외 테스트 시작");

            AppException exception = assertThrows(AppException.class, () ->
                    profileService.deleteProfileImage("nonexistent")
            );
            assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());

            log.debug("사용자 없음 삭제 예외 테스트 완료");
        }
    }
}
