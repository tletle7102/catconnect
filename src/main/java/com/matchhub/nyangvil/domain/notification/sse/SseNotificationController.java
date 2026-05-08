package com.matchhub.nyangvil.domain.notification.sse;

import com.matchhub.nyangvil.domain.user.model.entity.User;
import com.matchhub.nyangvil.domain.user.repository.UserRepository;
import com.matchhub.nyangvil.global.exception.AppException;
import com.matchhub.nyangvil.global.exception.Domain;
import com.matchhub.nyangvil.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/sse")
@RequiredArgsConstructor
public class SseNotificationController {

    private final SseEmitterService sseEmitterService;
    private final UserRepository userRepository;

    @GetMapping(value = "/notifications", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(Domain.USER, ErrorCode.USER_NOT_FOUND));
        return sseEmitterService.subscribe(user.getId());
    }
}
