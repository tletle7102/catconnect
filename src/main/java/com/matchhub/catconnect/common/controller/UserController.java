package com.matchhub.catconnect.common.controller;

import com.matchhub.catconnect.domain.user.repository.UserRepository;
import com.matchhub.catconnect.domain.user.model.entity.User;
import com.matchhub.catconnect.domain.user.model.enums.Role;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("currentPage", "users");
        return "user/users";
    }

    @GetMapping("/users/new")
    public String newUserForm(Model model) {
        model.addAttribute("currentPage", "users");
        return "user/user-form";
    }

    @PostMapping("/users")
    public String createUser(@RequestParam String username, @RequestParam String email) {
        User user = new User(username, email, Role.USER);
        userRepository.save(user);
        return "redirect:/users";
    }

    @GetMapping("/admin/users")
    public String adminListUsers(Model model) {
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("currentPage", "admin-users");
        return "user/admin-users";
    }


}
