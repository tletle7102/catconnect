package com.matchhub.catconnect.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matchhub.catconnect.domain.user.model.dto.UserRequestDTO;
import com.matchhub.catconnect.domain.user.model.dto.UserResponseDTO;
import com.matchhub.catconnect.domain.user.service.UserService;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    private UserResponseDTO testUser;

    @BeforeEach
    void setUp() {
        log.debug("테스트 설정 시작");

        // 기존 사용자 데이터 정리 (필요 시)
        List<Long> userIds = userService.getAllUsers().stream().map(UserResponseDTO::getId).toList();
        if (!userIds.isEmpty()) {
            userService.deleteUsers(userIds);
            log.debug("기존 사용자 정리 완료: count={}", userIds.size());
        }

        // 테스트용 사용자 생성
        UserRequestDTO requestDTO = new UserRequestDTO();
        requestDTO.setUsername("testUser");
        requestDTO.setEmail("test@example.com");
        requestDTO.setPassword("password");
        testUser = userService.createUser(requestDTO);

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
        @DisplayName("사용자 생성 성공")
        void testCreateUser() throws Exception {
            log.debug("사용자 생성 테스트 시작");

            UserRequestDTO requestDTO = new UserRequestDTO();
            requestDTO.setUsername("newUser");
            requestDTO.setEmail("new@example.com");
            requestDTO.setPassword("password");

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.username").value("newUser"))
                    .andDo(result -> log.debug("사용자 생성 응답: {}", result.getResponse().getContentAsString()));

            log.debug("사용자 생성 테스트 완료");
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
    }
}
