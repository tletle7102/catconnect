package com.matchhub.catconnect.domain.user.service;

import com.matchhub.catconnect.domain.user.model.dto.UserRequestDTO;
import com.matchhub.catconnect.domain.user.model.dto.UserResponseDTO;
import com.matchhub.catconnect.domain.user.repository.UserRepository;
import com.matchhub.catconnect.global.exception.AppException;
import com.matchhub.catconnect.global.exception.ErrorCode;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

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

    private UserResponseDTO testUser;

    @BeforeEach
    void setUp() {
        log.debug("테스트 설정 시작");

        // 테스트 전 DB 정리
        userRepository.deleteAll();

        // 테스트용 사용자 생성
        UserRequestDTO requestDTO = new UserRequestDTO();
        requestDTO.setUsername("testUser");
        requestDTO.setEmail("test@example.com");
        requestDTO.setPassword("password");
        testUser = userService.createUser(requestDTO);

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
    @DisplayName("사용자 CRUD 테스트")
    class UserCrudTests {

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

        @Test
        @DisplayName("사용자 생성 성공")
        void testCreateUser() {
            log.debug("사용자 생성 테스트 시작");

            // 새 사용자 요청 데이터 생성
            UserRequestDTO requestDTO = new UserRequestDTO();
            requestDTO.setUsername("newUser");
            requestDTO.setEmail("new@example.com");
            requestDTO.setPassword("password");

            // 사용자 생성
            UserResponseDTO user = userService.createUser(requestDTO);

            // 사용자 생성 확인
            assertNotNull(user.getId());
            assertEquals("newUser", user.getUsername());
            assertEquals("new@example.com", user.getEmail());
            assertNotNull(user.getCreatedDttm());

            log.debug("사용자 생성 테스트 완료");
        }

        @Test
        @DisplayName("중복 사용자 생성 실패")
        void testCreateDuplicateUser() {
            log.debug("중복 사용자 생성 테스트 시작");

            // 동일 사용자명으로 생성 시도
            UserRequestDTO requestDTO = new UserRequestDTO();
            requestDTO.setUsername("testUser");
            requestDTO.setEmail("different@example.com");
            requestDTO.setPassword("password");

            AppException exception = assertThrows(AppException.class, () ->
                    userService.createUser(requestDTO)
            );
            assertEquals(ErrorCode.USER_ALREADY_EXISTS, exception.getErrorCode());

            log.debug("중복 사용자 생성 테스트 완료");
        }

        @Test
        @DisplayName("중복 이메일 생성 실패")
        void testCreateDuplicateEmail() {
            log.debug("중복 이메일 생성 테스트 시작");

            // 동일 이메일로 생성 시도
            UserRequestDTO requestDTO = new UserRequestDTO();
            requestDTO.setUsername("differentUser");
            requestDTO.setEmail("test@example.com");
            requestDTO.setPassword("password");

            AppException exception = assertThrows(AppException.class, () ->
                    userService.createUser(requestDTO)
            );
            assertEquals(ErrorCode.USER_EMAIL_ALREADY_EXISTS, exception.getErrorCode());

            log.debug("중복 이메일 생성 테스트 완료");
        }

        @Test
        @DisplayName("사용자 수정 성공")
        void testUpdateUser() {
            log.debug("사용자 수정 테스트 시작");

            // 수정 요청 DTO 생성
            UserRequestDTO requestDTO = new UserRequestDTO();
            requestDTO.setUsername("updatedUser");
            requestDTO.setEmail("updated@example.com");
            requestDTO.setPassword("newPassword");

            // 사용자 수정
            UserResponseDTO updatedUser = userService.updateUser(testUser.getId(), requestDTO, "testUser");

            // 수정 내용 검증
            assertEquals("updatedUser", updatedUser.getUsername());
            assertEquals("updated@example.com", updatedUser.getEmail());
            // 수정 시간 갱신 확인
            assertNotNull(updatedUser.getCreatedDttm());

            log.debug("사용자 수정 테스트 완료");
        }

        @Test
        @DisplayName("사용자 수정 권한 없음 실패")
        void testUpdateUserUnauthorized() {
            log.debug("사용자 수정 권한 없음 테스트 시작");

            // 수정 요청 DTO 생성
            UserRequestDTO requestDTO = new UserRequestDTO();
            requestDTO.setUsername("updatedUser");
            requestDTO.setEmail("updated@example.com");
            requestDTO.setPassword("newPassword");

            // 다른 사용자로 인증 정보 변경
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken("otherUser", null, Collections.emptyList())
            );

            // 예외 발생 기대
            AppException exception = assertThrows(AppException.class, () ->
                    userService.updateUser(testUser.getId(), requestDTO, "otherUser")
            );
            assertEquals(ErrorCode.ACCESS_DENIED, exception.getErrorCode());

            log.debug("사용자 수정 권한 없음 테스트 완료");
        }

        @Test
        @DisplayName("사용자 수정 중복 이름 실패")
        void testUpdateUserDuplicateUsername() {
            log.debug("사용자 수정 중복 이름 테스트 시작");

            // 다른 사용자 생성
            UserRequestDTO otherUserDTO = new UserRequestDTO();
            otherUserDTO.setUsername("otherUser");
            otherUserDTO.setEmail("other@example.com");
            otherUserDTO.setPassword("password");
            userService.createUser(otherUserDTO);

            // 중복 사용자 이름으로 수정 시도
            UserRequestDTO requestDTO = new UserRequestDTO();
            requestDTO.setUsername("otherUser");
            requestDTO.setEmail("updated@example.com");
            requestDTO.setPassword("newPassword");

            AppException exception = assertThrows(AppException.class, () ->
                    userService.updateUser(testUser.getId(), requestDTO, "testUser")
            );
            assertEquals(ErrorCode.USER_ALREADY_EXISTS, exception.getErrorCode());

            log.debug("사용자 수정 중복 이름 테스트 완료");
        }

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
    }
}
