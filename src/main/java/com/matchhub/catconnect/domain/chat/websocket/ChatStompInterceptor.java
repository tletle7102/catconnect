package com.matchhub.catconnect.domain.chat.websocket;

import com.matchhub.catconnect.global.util.auth.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ChatStompInterceptor implements ChannelInterceptor {

    private static final Logger log = LoggerFactory.getLogger(ChatStompInterceptor.class);
    private final JwtProvider jwtProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = extractTokenFromHandshake(accessor);

            if (token != null && jwtProvider.validateToken(token)) {
                String username = jwtProvider.getUsernameFromToken(token);
                String role = jwtProvider.getRoleFromToken(token);

                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        username, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                );
                accessor.setUser(auth);
                log.debug("WebSocket 인증 성공: username={}", username);
            } else {
                log.warn("WebSocket 인증 실패: 유효하지 않은 토큰");
                throw new IllegalArgumentException("인증에 실패했습니다.");
            }
        }

        return message;
    }

    private String extractTokenFromHandshake(StompHeaderAccessor accessor) {
        // STOMP 헤더에서 토큰 추출
        List<String> authHeaders = accessor.getNativeHeader("Authorization");
        if (authHeaders != null && !authHeaders.isEmpty()) {
            String bearerToken = authHeaders.get(0);
            if (bearerToken.startsWith("Bearer ")) {
                return bearerToken.substring(7);
            }
        }

        // SockJS handshake 시 쿠키에서 추출
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        if (sessionAttributes != null) {
            Object cookieHeader = sessionAttributes.get("cookies");
            if (cookieHeader instanceof String cookies) {
                return extractTokenFromCookies(cookies);
            }
        }

        // native header에서 token 직접 추출
        List<String> tokenHeaders = accessor.getNativeHeader("token");
        if (tokenHeaders != null && !tokenHeaders.isEmpty()) {
            return tokenHeaders.get(0);
        }

        return null;
    }

    private String extractTokenFromCookies(String cookies) {
        String[] cookieParts = cookies.split(";");
        for (String cookie : cookieParts) {
            String trimmed = cookie.trim();
            if (trimmed.startsWith("jwtToken=")) {
                return trimmed.substring("jwtToken=".length());
            }
        }
        return null;
    }
}
