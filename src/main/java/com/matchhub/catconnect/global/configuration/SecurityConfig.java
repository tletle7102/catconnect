package com.matchhub.catconnect.global.configuration;

import com.matchhub.catconnect.global.exception.CustomAccessDeniedHandler;
import com.matchhub.catconnect.global.exception.CustomAuthenticationEntryPoint;
import com.matchhub.catconnect.global.util.auth.JwtAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    // JWT 인증 필터, 인증 실패 핸들러, 인가 실패 핸들러 의존성 주입
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    // 인증 없이 접근 허용할 URL 목록 (화이트리스트)
    private static final String[] WHITELIST = {
            "/swagger-ui/**",  // springdoc swagger ui
            "/api-docs/**",    // openapi 문서
            "/h2-console/**",  // H2 콘솔
            "/login",          // 로그인 페이지
            "/logout",         // 로그아웃 페이지
            "/signup",         // 회원가입 페이지
            "/verify-email",   // 이메일 인증 결과 페이지
            "/find-username",  // 아이디 찾기 페이지
            "/find-password",  // 비밀번호 찾기 페이지
            "/users/new",      // 회원 가입 (기존)
            "/boards",         // 게시판 리스트 등
            "/css/**",         // CSS 정적 리소스
            "/js/**",          // JS 정적 리소스
            "/api/auth/**",    // 인증 REST API ((로그인/로그아웃/인증상태확인/이메일인증)
            "/api/sms/**",     // SMS 인증 REST API
            "/favicon.ico"     // favicon
    };

    // 생성자에서 의존성 주입
    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            CustomAuthenticationEntryPoint authenticationEntryPoint,
            CustomAccessDeniedHandler accessDeniedHandler
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.debug("Spring Security 필터 체인 구성 시작");

        // CSRF 보호 비활성화 (API 서버에서는 주로 비활성화)
        http.csrf(csrf -> csrf.disable());
        log.debug("CSRF 보호 비활성화");

        // 세션을 서버에 저장하지 않고, JWT로 인증 상태 유지 (무상태 세션)
        http.sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );
        log.debug("세션 관리 설정: STATELESS");

        // URL 권한 설정
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(WHITELIST).permitAll()        // 화이트리스트는 인증없이 접근 허용
                .requestMatchers("/admin/**").hasRole("ADMIN") // /admin/** 경로는 ADMIN 권한만 접근 가능
                .requestMatchers("/users").hasRole("ADMIN")    // 사용자 목록은 ADMIN 권한만 접근 가능
                .requestMatchers("/boards/new", "/boards/**").authenticated() // 게시판 생성 및 상세는 인증 필요
                .requestMatchers("/api/likes/**", "/api/comments/**").authenticated() // 좋아요 및 댓글은 인증 필요
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/files/upload").authenticated() // 파일 업로드는 인증 필요
                .requestMatchers("/api/files/admin/**").hasRole("ADMIN") // 파일 관리자 삭제는 ADMIN 권한 필요 (DELETE 전에 선언)
                .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/files/**").authenticated() // 일반 파일 삭제는 인증 필요
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/users/**").hasRole("ADMIN") // 사용자 조회는 ADMIN 권한만 접근 가능
                .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/users/**").hasRole("ADMIN") // 사용자 삭제는 ADMIN 권한만 접근 가능
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/users").permitAll() // 회원가입은 모두 허용
                .anyRequest().permitAll()                      // 나머지 요청은 모두 허용
        );
        log.debug("요청 권한 설정 완료: whitelist={}, admin=/admin/**,/users,/api/files/admin/**,GET/DELETE /api/users/**, authenticated=/boards/new,/boards/**,/api/likes/**,/api/comments/**,POST /api/files/upload,DELETE /api/files/**",
                String.join(",", WHITELIST));

        // 인증 실패, 인가 실패 시 처리 핸들러 설정
        http.exceptionHandling(exception -> exception
                .authenticationEntryPoint(authenticationEntryPoint) // 인증 실패 시 동작
                .accessDeniedHandler(accessDeniedHandler)             // 인가 실패 시 동작
        );
        log.debug("예외 처리 설정: authenticationEntryPoint={}, accessDeniedHandler={}",
                authenticationEntryPoint.getClass().getSimpleName(),
                accessDeniedHandler.getClass().getSimpleName());

        // 기본 폼 로그인 비활성화 (JWT 인증을 사용하므로 필요 없음)
        http.formLogin(form -> form.disable());
        log.debug("폼 로그인 비활성화");

        // 로그아웃 설정
        http.logout(logout -> logout
                .logoutUrl("/logout")           // 로그아웃 요청 URL
                .logoutSuccessUrl("/")          // 로그아웃 성공 후 이동할 URL
                .deleteCookies("jwtToken")      // 로그아웃 시 JWT 쿠키 삭제
                .permitAll()                    // 모두 접근 허용
                .addLogoutHandler((request, response, authentication) -> {
                    log.debug("로그아웃 처리: username={}", authentication != null ? authentication.getName() : "없음");
                })
                .logoutSuccessHandler((request, response, authentication) -> {
                    log.debug("로그아웃 성공, 리다이렉트: url=/");
                    response.sendRedirect("/");
                })
        );
        log.debug("로그아웃 설정: logoutUrl=/logout, logoutSuccessUrl=/, deleteCookies=jwtToken");

        // H2 콘솔 iframe 접근 허용 (기본 보안 설정 해제)
        http.headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.sameOrigin())
        );
        log.debug("헤더 설정: frameOptions=sameOrigin");

        // JWT 필터 스프링 시큐리티 UsernamePasswordAuthenticationFilter 앞에 추가
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        log.debug("JWT 필터 추가: JwtAuthenticationFilter");

        log.debug("Spring Security 필터 체인 구성 완료");
        return http.build();
    }

    // AuthenticationManager 빈 등록 (인증 매니저 사용을 위해 필요)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        log.debug("AuthenticationManager 빈 생성");
        return authenticationConfiguration.getAuthenticationManager();
    }

    // 비밀번호 암호화를 위한 BCryptPasswordEncoder 빈 등록
    @Bean
    public PasswordEncoder passwordEncoder() {
        log.debug("BCryptPasswordEncoder 빈 생성");
        return new BCryptPasswordEncoder();
    }
}
