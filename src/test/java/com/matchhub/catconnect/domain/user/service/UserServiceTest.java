package com.matchhub.catconnect.domain.user.service;

import com.matchhub.catconnect.domain.user.model.dto.UserResponseDTO;
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
import org.springframework.data.domain.Page;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserService의 통합 테스트 클래스
 * 사용자 관련 비즈니스 로직을 테스트하며, 실제 H2 DB와 연동
 */
@DisplayName("UserService 테스트")
@SpringBootTest
class UserServiceTest {

    private static final Logger log = LoggerFactory.getLogger(UserServiceTest.class);

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;

    @BeforeEach
    void setUp() {
        log.debug("테스트 설정 시작");

        // 테스트 전 DB 정리
        userRepository.deleteAll();

        // 테스트용 사용자 생성 (Repository 직접 사용)
        testUser = new User("testUser", "test@example.com", passwordEncoder.encode("password"), Role.USER);
        userRepository.save(testUser);

        // 인증 정보 설정
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("testUser", null, Collections.emptyList())
        );

        log.debug("테스트 설정 완료: userId={}", testUser.getId());
    }

    @AfterEach
    void tearDown() {
        log.debug("테스트 정리 시작");
        try {
            userRepository.deleteAll();
            SecurityContextHolder.clearContext();
            log.debug("테스트 정리 완료");
        } catch (Exception e) {
            log.debug("테스트 정리 실패: {}", e.getMessage());
        }
    }

    @Nested
    @DisplayName("사용자 조회 테스트")
    class UserReadTests {

        @Test
        @DisplayName("전체 사용자 조회 성공")
        void testGetAllUsers() {
            log.debug("전체 사용자 조회 테스트 시작");

            // 전체 사용자 조회
            List<UserResponseDTO> users = userService.getAllUsers();

            // 사용자 목록 확인
            assertFalse(users.isEmpty());
            assertTrue(users.stream().anyMatch(user -> user.getUsername().equals("testUser")));

            log.debug("전체 사용자 조회 테스트 완료");
        }

        @Test
        @DisplayName("전체 사용자 페이지네이션 조회 성공")
        void testGetAllUsersPaginated() {
            log.debug("전체 사용자 페이지네이션 조회 테스트 시작");

            // 추가 사용자 생성
            for (int i = 0; i < 15; i++) {
                User user = new User("user" + i, "user" + i + "@example.com", passwordEncoder.encode("password"), Role.USER);
                userRepository.save(user);
            }

            // 첫 번째 페이지 조회 (size=10)
            Page<UserResponseDTO> page = userService.getAllUsers(0, 10);

            // 페이지네이션 확인
            assertEquals(10, page.getSize());
            assertEquals(16, page.getTotalElements()); // testUser + 15명
            assertEquals(2, page.getTotalPages());

            log.debug("전체 사용자 페이지네이션 조회 테스트 완료");
        }

        @Test
        @DisplayName("사용자 상세 조회 성공")
        void testGetUserById() {
            log.debug("사용자 상세 조회 테스트 시작");

            // 사용자 ID로 상세 조회
            UserResponseDTO user = userService.getUserById(testUser.getId());

            // 사용자 정보 확인
            assertEquals("testUser", user.getUsername());
            assertEquals("test@example.com", user.getEmail());

            log.debug("사용자 상세 조회 테스트 완료");
        }

        @Test
        @DisplayName("사용자 상세 조회 실패 - 사용자 없음")
        void testGetUserByIdNotFound() {
            log.debug("사용자 상세 조회 실패 테스트 시작");

            // 존재하지 않는 ID로 조회
            AppException exception = assertThrows(AppException.class, () ->
                    userService.getUserById(999L)
            );
            assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());

            log.debug("사용자 상세 조회 실패 테스트 완료");
        }
    }

    @Nested
    @DisplayName("사용자 삭제 테스트")
    class UserDeleteTests {

        @Test
        @DisplayName("사용자 삭제 성공")
        void testDeleteUser() {
            log.debug("사용자 삭제 테스트 시작");

            // 사용자 삭제
            userService.deleteUser(testUser.getId());

            // 사용자 삭제 확인
            assertFalse(userRepository.existsById(testUser.getId()));

            log.debug("사용자 삭제 테스트 완료");
        }

        @Test
        @DisplayName("사용자 삭제 실패 - 사용자 없음")
        void testDeleteUserNotFound() {
            log.debug("사용자 삭제 실패 테스트 시작");

            // 존재하지 않는 ID로 삭제 시도
            AppException exception = assertThrows(AppException.class, () ->
                    userService.deleteUser(999L)
            );
            assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());

            log.debug("사용자 삭제 실패 테스트 완료");
        }

        @Test
        @DisplayName("여러 사용자 삭제 성공")
        void testDeleteUsers() {
            log.debug("여러 사용자 삭제 테스트 시작");

            // 추가 사용자 생성
            User user1 = new User("user1", "user1@example.com", passwordEncoder.encode("password"), Role.USER);
            User user2 = new User("user2", "user2@example.com", passwordEncoder.encode("password"), Role.USER);
            userRepository.save(user1);
            userRepository.save(user2);

            // 여러 사용자 삭제
            List<Long> ids = List.of(user1.getId(), user2.getId());
            userService.deleteUsers(ids);

            // 삭제 확인
            assertFalse(userRepository.existsById(user1.getId()));
            assertFalse(userRepository.existsById(user2.getId()));
            // testUser는 그대로 유지
            assertTrue(userRepository.existsById(testUser.getId()));

            log.debug("여러 사용자 삭제 테스트 완료");
        }

        @Test
        @DisplayName("빈 목록으로 삭제 시도 - 예외 없이 처리")
        void testDeleteUsersEmptyList() {
            log.debug("빈 목록으로 삭제 테스트 시작");

            // 빈 목록으로 삭제 시도
            assertDoesNotThrow(() -> userService.deleteUsers(Collections.emptyList()));
            assertDoesNotThrow(() -> userService.deleteUsers(null));

            log.debug("빈 목록으로 삭제 테스트 완료");
        }
    }
}
