package com.matchhub.catconnect.domain.user.controller;

import com.matchhub.catconnect.domain.user.model.dto.UserResponseDTO;
import com.matchhub.catconnect.domain.user.model.entity.User;
import com.matchhub.catconnect.domain.user.model.enums.Role;
import com.matchhub.catconnect.domain.user.repository.UserRepository;
import com.matchhub.catconnect.domain.user.service.UserService;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * UserRestController의 통합 테스트 클래스
 * 사용자 관련 REST API 엔드포인트를 테스트하며, 실제 DB와 연동
 */
@DisplayName("UserRestController 테스트")
@SpringBootTest
@AutoConfigureMockMvc
class UserRestControllerTest {

    private static final Logger log = LoggerFactory.getLogger(UserRestControllerTest.class);

    @Autowired
    private MockMvc mockMvc;

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

        // 기존 사용자 데이터 정리 (필요 시)
        List<Long> userIds = userService.getAllUsers().stream().map(UserResponseDTO::getId).toList();
        if (!userIds.isEmpty()) {
            userService.deleteUsers(userIds);
            log.debug("기존 사용자 정리 완료: count={}", userIds.size());
        }

        // 테스트용 사용자 생성 (Repository 직접 사용)
        testUser = new User("testUser", "test@example.com", passwordEncoder.encode("password"), Role.USER);
        userRepository.save(testUser);

        log.debug("테스트 설정 완료: userId={}", testUser.getId());
    }

    @AfterEach
    void tearDown() {
        log.debug("테스트 정리 시작");

        // 테스트용 사용자 정리
        List<Long> userIds = userService.getAllUsers().stream().map(UserResponseDTO::getId).toList();
        if (!userIds.isEmpty()) {
            userService.deleteUsers(userIds);
            log.debug("사용자 정리 완료: count={}", userIds.size());
        }

        log.debug("테스트 정리 완료");
    }

    @Nested
    @DisplayName("사용자 API 테스트")
    class UserApiTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("전체 사용자 조회 성공")
        void testGetAllUsers() throws Exception {
            log.debug("전체 사용자 조회 테스트 시작");

            mockMvc.perform(get("/api/users")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.content[0].username").value("testUser"))
                    .andDo(result -> log.debug("전체 사용자 조회 응답: {}", result.getResponse().getContentAsString()));

            log.debug("전체 사용자 조회 테스트 완료");
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("사용자 상세 조회 성공")
        void testGetUserById() throws Exception {
            log.debug("사용자 상세 조회 테스트 시작");

            mockMvc.perform(get("/api/users/" + testUser.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.username").value("testUser"))
                    .andDo(result -> log.debug("사용자 상세 조회 응답: {}", result.getResponse().getContentAsString()));

            log.debug("사용자 상세 조회 테스트 완료");
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("사용자 삭제 성공")
        void testDeleteUser() throws Exception {
            log.debug("사용자 삭제 테스트 시작");

            mockMvc.perform(delete("/api/users/" + testUser.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andExpect(jsonPath("$.message").value("사용자 삭제 성공"))
                    .andDo(result -> log.debug("사용자 삭제 응답: {}", result.getResponse().getContentAsString()));

            log.debug("사용자 삭제 테스트 완료");
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("사용자 수정 성공")
        void testUpdateUser() throws Exception {
            log.debug("사용자 수정 테스트 시작");

            String requestBody = """
					{
						"username": "updatedUser",
						"email": "updated@gmail.com",
						"phoneNumber": "01012345678",
						"password": "newPassword123"
					}
					""";

            mockMvc.perform(put("/api/users/" + testUser.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andExpect(jsonPath("$.message").value("사용자 수정 성공"))
                    .andExpect(jsonPath("$.data.username").value("updatedUser"))
                    .andExpect(jsonPath("$.data.email").value("updated@gmail.com"))
                    .andExpect(jsonPath("$.data.phoneNumber").value("01012345678"))
                    .andDo(result -> log.debug("사용자 수정 응답: {}", result.getResponse().getContentAsString()));

            log.debug("사용자 수정 테스트 완료");
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("사용자 일부 정보만 수정 성공")
        void testUpdateUserPartial() throws Exception {
            log.debug("사용자 일부 정보 수정 테스트 시작");

            // 휴대폰 번호만 수정
            String requestBody = """
					{
						"phoneNumber": "01098765432"
					}
					""";

            mockMvc.perform(put("/api/users/" + testUser.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.username").value("testUser")) // 기존 값 유지
                    .andExpect(jsonPath("$.data.email").value("test@example.com")) // 기존 값 유지
                    .andExpect(jsonPath("$.data.phoneNumber").value("01098765432")) // 변경됨
                    .andDo(result -> log.debug("사용자 일부 수정 응답: {}", result.getResponse().getContentAsString()));

            log.debug("사용자 일부 정보 수정 테스트 완료");
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("사용자 수정 실패 - 사용자 없음")
        void testUpdateUserNotFound() throws Exception {
            log.debug("사용자 수정 실패 테스트 시작");

            String requestBody = """
					{
						"username": "newName"
					}
					""";

            mockMvc.perform(put("/api/users/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("USER_001"))
                    .andDo(result -> log.debug("사용자 수정 실패 응답: {}", result.getResponse().getContentAsString()));

            log.debug("사용자 수정 실패 테스트 완료");
        }
    }
}
