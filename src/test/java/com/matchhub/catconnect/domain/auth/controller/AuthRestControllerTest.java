package com.matchhub.catconnect.domain.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matchhub.catconnect.domain.auth.model.dto.LoginRequestDTO;
import com.matchhub.catconnect.domain.auth.model.dto.TokenRefreshRequestDTO;
import com.matchhub.catconnect.domain.auth.repository.RefreshTokenRepository;
import com.matchhub.catconnect.domain.user.model.entity.User;
import com.matchhub.catconnect.domain.user.model.enums.Role;
import com.matchhub.catconnect.domain.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthRestController 통합 테스트
 *
 * 로그인, 토큰 재발급, 로그아웃 API 테스트
 */
@DisplayName("AuthRestController 테스트")
@SpringBootTest
@AutoConfigureMockMvc
class AuthRestControllerTest {

    private static final Logger log = LoggerFactory.getLogger(AuthRestControllerTest.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

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

        log.debug("테스트 설정 완료: userId={}, username={}", testUser.getId(), testUser.getUsername());
    }

    @AfterEach
    void tearDown() {
        log.debug("테스트 정리 시작");
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
        log.debug("테스트 정리 완료");
    }

    @Nested
    @DisplayName("로그인 API 테스트")
    class LoginTests {

        @Test
        @DisplayName("로그인 성공 - Access Token과 Refresh Token 발급")
        void testLoginSuccess() throws Exception {
            // given
            LoginRequestDTO requestDTO = new LoginRequestDTO();
            requestDTO.setUsername("testuser");
            requestDTO.setPassword("password123");
            requestDTO.setStayLoggedIn(false);

            // when & then
            MvcResult result = mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.username").value("testuser"))
                    .andExpect(jsonPath("$.data.role").value("USER"))
                    .andExpect(jsonPath("$.data.token").isNotEmpty())
                    .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                    .andExpect(jsonPath("$.data.authenticated").value(true))
                    .andReturn();

            // 쿠키 확인
            Cookie jwtCookie = result.getResponse().getCookie("jwtToken");
            Cookie refreshCookie = result.getResponse().getCookie("refreshToken");

            assertNotNull(jwtCookie);
            assertNotNull(refreshCookie);

            // DB에 Refresh Token 저장 확인
            assertEquals(1, refreshTokenRepository.count());

            log.debug("로그인 성공 테스트 완료");
        }

        @Test
        @DisplayName("로그인 실패 - 잘못된 비밀번호")
        void testLoginFailureWrongPassword() throws Exception {
            // given
            LoginRequestDTO requestDTO = new LoginRequestDTO();
            requestDTO.setUsername("testuser");
            requestDTO.setPassword("wrongpassword");

            // when & then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.result").value("ERROR"));

            log.debug("로그인 실패 테스트 완료");
        }

        @Test
        @DisplayName("로그인 실패 - 존재하지 않는 사용자")
        void testLoginFailureUserNotFound() throws Exception {
            // given
            LoginRequestDTO requestDTO = new LoginRequestDTO();
            requestDTO.setUsername("nonexistent");
            requestDTO.setPassword("password123");

            // when & then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isUnauthorized());

            log.debug("존재하지 않는 사용자 로그인 실패 테스트 완료");
        }
    }

    @Nested
    @DisplayName("토큰 재발급 API 테스트")
    class RefreshTokenTests {

        @Test
        @DisplayName("토큰 재발급 성공 - 쿠키로 Refresh Token 전달")
        void testRefreshTokenWithCookie() throws Exception {
            // given - 먼저 로그인
            LoginRequestDTO loginRequest = new LoginRequestDTO();
            loginRequest.setUsername("testuser");
            loginRequest.setPassword("password123");

            MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andReturn();

            Cookie refreshCookie = loginResult.getResponse().getCookie("refreshToken");
            assertNotNull(refreshCookie);

            // when & then - 토큰 재발급
            mockMvc.perform(post("/api/auth/refresh")
                            .cookie(refreshCookie))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                    .andExpect(jsonPath("$.data.username").value("testuser"))
                    .andExpect(jsonPath("$.data.role").value("USER"));

            log.debug("토큰 재발급 성공 테스트 완료 (쿠키)");
        }

        @Test
        @DisplayName("토큰 재발급 성공 - 요청 바디로 Refresh Token 전달")
        void testRefreshTokenWithBody() throws Exception {
            // given - 먼저 로그인
            LoginRequestDTO loginRequest = new LoginRequestDTO();
            loginRequest.setUsername("testuser");
            loginRequest.setPassword("password123");

            MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andReturn();

            String responseBody = loginResult.getResponse().getContentAsString();
            String refreshToken = objectMapper.readTree(responseBody)
                    .path("data").path("refreshToken").asText();

            // when & then - 토큰 재발급
            TokenRefreshRequestDTO refreshRequest = new TokenRefreshRequestDTO(refreshToken);

            mockMvc.perform(post("/api/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(refreshRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.accessToken").isNotEmpty());

            log.debug("토큰 재발급 성공 테스트 완료 (바디)");
        }

        @Test
        @DisplayName("토큰 재발급 실패 - Refresh Token 없음")
        void testRefreshTokenMissing() throws Exception {
            // when & then
            mockMvc.perform(post("/api/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.result").value("ERROR"));

            log.debug("Refresh Token 없음 테스트 완료");
        }

        @Test
        @DisplayName("토큰 재발급 실패 - 유효하지 않은 Refresh Token")
        void testRefreshTokenInvalid() throws Exception {
            // given
            TokenRefreshRequestDTO refreshRequest = new TokenRefreshRequestDTO("invalid.refresh.token");

            // when & then
            mockMvc.perform(post("/api/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(refreshRequest)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.result").value("ERROR"));

            log.debug("유효하지 않은 Refresh Token 테스트 완료");
        }
    }

    @Nested
    @DisplayName("로그아웃 API 테스트")
    class LogoutTests {

        @Test
        @DisplayName("로그아웃 성공 - DB에서 Refresh Token 삭제")
        void testLogoutSuccess() throws Exception {
            // given - 먼저 로그인
            LoginRequestDTO loginRequest = new LoginRequestDTO();
            loginRequest.setUsername("testuser");
            loginRequest.setPassword("password123");

            MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andReturn();

            Cookie jwtCookie = loginResult.getResponse().getCookie("jwtToken");
            Cookie refreshCookie = loginResult.getResponse().getCookie("refreshToken");

            // DB에 Refresh Token 존재 확인
            assertEquals(1, refreshTokenRepository.count());

            // when & then - 로그아웃
            MvcResult logoutResult = mockMvc.perform(post("/api/auth/logout")
                            .cookie(jwtCookie, refreshCookie))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andExpect(jsonPath("$.message").value("로그아웃 성공"))
                    .andReturn();

            // DB에서 Refresh Token 삭제 확인
            assertEquals(0, refreshTokenRepository.count());

            // 쿠키 삭제 확인 (Max-Age가 0으로 설정됨)
            Cookie deletedJwtCookie = logoutResult.getResponse().getCookie("jwtToken");
            Cookie deletedRefreshCookie = logoutResult.getResponse().getCookie("refreshToken");

            assertNotNull(deletedJwtCookie);
            assertEquals(0, deletedJwtCookie.getMaxAge());

            assertNotNull(deletedRefreshCookie);
            assertEquals(0, deletedRefreshCookie.getMaxAge());

            log.debug("로그아웃 성공 테스트 완료");
        }
    }
}
