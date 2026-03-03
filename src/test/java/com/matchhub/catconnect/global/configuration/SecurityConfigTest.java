package com.matchhub.catconnect.global.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("SecurityConfig 테스트")
@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfigTest.class);

    @Autowired
    private MockMvc mockMvc; // HTTP 요청 시뮬레이션용 객체

    @Autowired
    private ObjectMapper objectMapper; // JSON 변환용 객체

    @Nested
    @DisplayName("Security 설정 테스트")
    class SecurityConfigurationTests {

        @Test
        @DisplayName("화이트리스트 경로 인증 없이 접근 가능")
        void testWhitelistAccess() throws Exception {
            log.debug("화이트리스트 테스트 시작");
            // 인증 없이 접근 허용된 경로들 (/users는 ADMIN 전용으로 변경됨)
            String[] whitelist = {"/login", "/boards", "/api/auth/check"};
            for (String path : whitelist) {
                // 각 경로에 대해 GET 요청을 보내고 200 OK 상태 확인
                mockMvc.perform(get(path))
                        .andExpect(status().isOk())
                        .andDo(result -> log.debug("화이트리스트 경로 {} 접근 성공", path));
            }
            log.debug("화이트리스트 테스트 완료");
        }

        @Test
        @DisplayName("사용자 목록 경로 비인증 접근 시 401 반환")
        void testUsersPathWithoutAuth() throws Exception {
            log.debug("사용자 목록 경로 테스트 시작");
            // /users 경로는 ADMIN 권한 필요, 비인증 시 401 반환
            mockMvc.perform(get("/users"))
                    .andExpect(status().isUnauthorized())
                    .andDo(result -> log.debug("사용자 목록 비인증 접근 거부됨"));
            log.debug("사용자 목록 경로 테스트 완료");
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("사용자 목록 경로 일반 사용자 접근 시 403 반환")
        void testUsersPathWithUserRole() throws Exception {
            log.debug("사용자 목록 일반 사용자 테스트 시작");
            // /users 경로는 ADMIN 권한 필요, USER 역할로 접근 시 403 반환
            mockMvc.perform(get("/users"))
                    .andExpect(status().isForbidden())
                    .andDo(result -> log.debug("사용자 목록 일반 사용자 접근 거부됨"));
            log.debug("사용자 목록 일반 사용자 테스트 완료");
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("사용자 목록 경로 관리자 접근 성공")
        void testUsersPathWithAdminRole() throws Exception {
            log.debug("사용자 목록 관리자 테스트 시작");
            // /users 경로는 ADMIN 권한으로 접근 가능
            mockMvc.perform(get("/users"))
                    .andExpect(status().isOk())
                    .andDo(result -> log.debug("사용자 목록 관리자 접근 성공"));
            log.debug("사용자 목록 관리자 테스트 완료");
        }

        @Test
        @DisplayName("인증 필요 경로 비인증 접근 시 401 반환")
        void testAuthenticatedPathWithoutAuth() throws Exception {
            log.debug("인증 필요 경로 테스트 시작");
            // 인증이 필요한 경로에 인증 없이 접근 시도
            mockMvc.perform(get("/boards/new"))
                    .andExpect(status().isUnauthorized()) // 401 인증 실패 상태 코드 기대
                    .andExpect(content().contentType("application/json;charset=UTF-8")) // JSON 응답 타입 확인
                    // JSON 응답 내용에 인증 실패 도메인, 코드, 메시지, 상태 포함 확인
                    .andExpect(jsonPath("$.domain").value("AUTH"))
                    .andExpect(jsonPath("$.code").value("AUTH_001"))
                    .andExpect(jsonPath("$.message").value("인증에 실패했습니다: Full authentication is required to access this resource"))
                    .andExpect(jsonPath("$.status").value(401))
                    .andDo(result -> log.debug("인증 실패 응답: {}", result.getResponse().getContentAsString()));
            log.debug("인증 필요 경로 테스트 완료");
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("인가 필요 경로 사용자 역할로 접근 시 403 반환")
        void testAdminPathWithUserRole() throws Exception {
            log.debug("인가 필요 경로 테스트 시작");
            // USER 권한으로 ADMIN 권한이 필요한 경로 접근 시도
            mockMvc.perform(get("/admin/boards"))
                    .andExpect(status().isForbidden()) // 403 접근 거부 상태 코드 기대
                    .andExpect(content().contentType("application/json;charset=UTF-8")) // JSON 응답 타입 확인
                    // JSON 응답 내용에 인가 실패 도메인, 코드, 메시지, 상태 포함 확인
                    .andExpect(jsonPath("$.domain").value("AUTH"))
                    .andExpect(jsonPath("$.code").value("AUTH_002"))
                    .andExpect(jsonPath("$.message").value("접근 권한이 없습니다: Access Denied"))
                    .andExpect(jsonPath("$.status").value(403))
                    .andDo(result -> log.debug("인가 실패 응답: {}", result.getResponse().getContentAsString()));
            log.debug("인가 필요 경로 테스트 완료");
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("인가 필요 경로 관리자 역할로 접근 성공")
        void testAdminPathWithAdminRole() throws Exception {
            log.debug("관리자 경로 테스트 시작");
            // ADMIN 권한으로 접근 허용된 경로 접근 시도
            mockMvc.perform(get("/admin/boards"))
                    .andExpect(status().isOk()) // 200 OK 상태 기대
                    .andDo(result -> log.debug("관리자 경로 접근 성공"));
            log.debug("관리자 경로 테스트 완료");
        }
    }
}
