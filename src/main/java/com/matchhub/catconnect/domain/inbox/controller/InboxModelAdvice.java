package com.matchhub.catconnect.domain.inbox.controller;

import com.matchhub.catconnect.domain.inbox.repository.InboxItemRepository;
import com.matchhub.catconnect.domain.user.model.entity.User;
import com.matchhub.catconnect.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class InboxModelAdvice {

    private final InboxItemRepository inboxItemRepository;
    private final UserRepository userRepository;

    @ModelAttribute("unreadCount")
    public Long unreadCount() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return 0L;
        }

        try {
            String username = auth.getName();
            return userRepository.findByUsername(username)
                    .map(user -> inboxItemRepository.countUnread(user.getId()))
                    .orElse(0L);
        } catch (Exception e) {
            return 0L;
        }
    }
}
