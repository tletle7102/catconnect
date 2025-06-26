package com.matchhub.catconnect.global.configuration;

import com.matchhub.catconnect.global.util.auth.JwtAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        logger.debug("Spring Security 필터 체인 구성 시작");

        // 1. CSRF 비활성화
        http.csrf(csrf -> csrf.disable());
        logger.debug("CSRF 보호 비활성화");

        // 2. 세션 관리 설정
        http.sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );
        logger.debug("세션 관리 설정: STATELESS");

        // 3. 요청 권한 설정
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/h2-console/**", "/login", "/logout", "/users/new", "/users", "/boards", "/css/**", "/js/**", "/auth/check").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/boards/new", "/boards/**").authenticated()
                .anyRequest().permitAll()
        );
        logger.debug("요청 권한 설정 완료");

        // 4. 폼 로그인 비활성화
        http.formLogin(form -> form.disable());
        logger.debug("폼 로그인 비활성화");

        // 5. 로그아웃 설정
        http.logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .deleteCookies("jwtToken")
                .permitAll()
                .addLogoutHandler((request, response, authentication) -> {
                    logger.debug("로그아웃 처리: username={}", authentication != null ? authentication.getName() : "없음");
                })
                .logoutSuccessHandler((request, response, authentication) -> {
                    logger.debug("로그아웃 성공, 리다이렉트: url=/");
                    response.sendRedirect("/");
                })
        );
        logger.debug("로그아웃 설정: logoutUrl=/logout, logoutSuccessUrl=/, deleteCookies=jwtToken");

        // 6. H2 콘솔 iframe 허용
        http.headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.sameOrigin())
        );
        logger.debug("헤더 설정: frameOptions=sameOrigin");

        // 7. JWT 필터 추가
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        logger.debug("JWT 필터 추가: JwtAuthenticationFilter");

        logger.debug("Spring Security 필터 체인 구성 완료");

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        logger.debug("AuthenticationManager 빈 생성");
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        logger.debug("BCryptPasswordEncoder 빈 생성");
        return new BCryptPasswordEncoder();
    }
}
