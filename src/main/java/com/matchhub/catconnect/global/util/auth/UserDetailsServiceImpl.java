package com.matchhub.catconnect.global.util.auth;

import com.matchhub.catconnect.domain.user.model.entity.User;
import com.matchhub.catconnect.domain.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);
    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.debug("사용자 조회 시도: username={}", username);
        User user = findUserByUsername(username);

        logger.debug(
                "사용자 조회 성공: username={}, role={}",
                user.getUsername(),
                user.getRole().name()
        );
        logger.debug("암호화된 비밀번호 로드: username={}", user.getUsername());

        String roleName = "ROLE_" + user.getRole().name();
        logger.debug("권한 이름 생성: roleName={}", roleName);
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(roleName);
        List<SimpleGrantedAuthority> authorities = Collections.singletonList(authority);
        logger.debug("권한 리스트 생성: authorities={}", authorities);

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities
        );
        logger.debug(
                "UserDetails 생성 완료: username={}, authorities={}",
                userDetails.getUsername(),
                userDetails.getAuthorities());
        logger.debug(
                "UserDetails 상태: isEnabled={}, isAccountNonExpired={}, isCredentialsNonExpired={}, isAccountNonLocked={}",
                userDetails.isEnabled(),
                userDetails.isAccountNonExpired(),
                userDetails.isCredentialsNonExpired(),
                userDetails.isAccountNonLocked());

        return userDetails;
    }

    private User findUserByUsername(String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            logger.error("사용자 조회 실패: username={}", username);
            throw new UsernameNotFoundException("다음 사용자를 찾지 못했습니다: " + username);
        }
        logger.debug("사용자 조회 완료: username={}", username);
        return userOptional.get();
    }
}
