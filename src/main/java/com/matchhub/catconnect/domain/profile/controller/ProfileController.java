package com.matchhub.catconnect.domain.profile.controller;

import com.matchhub.catconnect.domain.user.model.dto.UserResponseDTO;
import com.matchhub.catconnect.domain.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 프로필 설정 페이지 뷰 컨트롤러
 */
@Controller
@RequestMapping("/profile")
public class ProfileController {

    private static final Logger log = LoggerFactory.getLogger(ProfileController.class);

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String profilePage(Authentication authentication, Model model) {
        String username = authentication.getName();
        log.debug("GET /profile 요청: username={}", username);

        UserResponseDTO user = userService.getUserByUsername(username);
        model.addAttribute("user", user);

        return "profile/profile-settings";
    }

    @GetMapping("/notification-settings")
    public String notificationSettingsPage() {
        return "profile/notification-settings";
    }
}
