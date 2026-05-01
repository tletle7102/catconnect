package com.matchhub.catconnect.domain.chat.controller;

import com.matchhub.catconnect.domain.user.model.entity.User;
import com.matchhub.catconnect.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class ChatViewController {

    private final UserRepository userRepository;

    @GetMapping("/chat/{roomId}")
    public String chatRoom(@PathVariable Long roomId, Model model, Authentication authentication) {
        if (authentication != null) {
            User user = userRepository.findByUsername(authentication.getName()).orElse(null);
            model.addAttribute("currentUser", user);
        }
        model.addAttribute("roomId", roomId);
        return "chat/chat-room";
    }

    @GetMapping("/inbox")
    public String inbox(Model model, Authentication authentication) {
        if (authentication != null) {
            User user = userRepository.findByUsername(authentication.getName()).orElse(null);
            model.addAttribute("currentUser", user);
        }
        return "inbox/inbox";
    }
}
