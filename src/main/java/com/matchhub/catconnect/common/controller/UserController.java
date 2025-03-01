package com.matchhub.catconnect.common.controller;

import com.matchhub.catconnect.domain.user.model.entity.UserEntity;
import com.matchhub.catconnect.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/signup")
    public String showSignupForm() {
        return "signup";
    }

    @PostMapping("/signup")
    public String signup(@RequestParam String username,
                         @RequestParam String password,
                         @RequestParam String nickname) {

        UserEntity user = new UserEntity(username, password, nickname);
        userRepository.save(user);
        return "redirect:/home";
    }

    @GetMapping("/home")
    public String home() {

        return "home";
    }
}
