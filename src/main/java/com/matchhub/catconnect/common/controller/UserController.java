package com.matchhub.catconnect.common.controller;

import com.matchhub.catconnect.domain.user.repository.UserRepository;
import com.matchhub.catconnect.domain.user.model.entity.User;
import com.matchhub.catconnect.domain.user.model.enums.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        logger.debug("사용자 목록 조회 요청");
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("currentPage", "users");
        logger.debug("사용자 목록 조회 완료: size={}", userRepository.findAll().size());
        return "user/users";
    }

    @GetMapping("/users/new")
    public String newUserForm(Model model) {
        logger.debug("새 사용자 폼 요청");
        model.addAttribute("currentPage", "users");
        return "user/user-form";
    }

    @PostMapping("/users")
    public String createUser(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password,
            RedirectAttributes redirectAttributes
    ) {
        logger.debug("사용자 생성 요청: username={}, email={}", username, email);
        try {
            if (userRepository.findByUsername(username).isPresent()) {
                logger.warn("사용자 이름 중복: username={}", username);
                redirectAttributes.addFlashAttribute("error", "이미 존재하는 사용자 이름입니다.");
                return "redirect:/users/new";
            }
            if (userRepository.findByEmail(email).isPresent()) {
                logger.warn("이메일 중복: email={}", email);
                redirectAttributes.addFlashAttribute("error", "이미 존재하는 이메일입니다.");
                return "redirect:/users/new";
            }
            User user = new User(username, email, passwordEncoder.encode(password), Role.USER);
            userRepository.save(user);
            logger.debug("사용자 생성 완료: id={}", user.getId());
            return "redirect:/users";
        } catch (Exception e) {
            logger.error("사용자 생성 실패: error={}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "사용자 생성 중 오류가 발생했습니다.");
            return "redirect:/users/new";
        }
    }

    @GetMapping("/admin/users")
    public String adminListUsers(Model model) {
        logger.debug("관리자 사용자 목록 조회 요청");
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("currentPage", "admin-users");
        logger.debug("관리자 사용자 목록 조회 완료: size={}", userRepository.findAll().size());
        return "user/admin-users";
    }

    @PostMapping("/admin/users/delete")
    public String deleteUsers(@RequestParam(value = "ids", required = false) List<Long> ids) {
        logger.debug("사용자 다중 삭제 요청: ids={}", ids);
        try {
            if (ids == null || ids.isEmpty()) {
                logger.warn("삭제할 사용자 ID 없음");
                String encodedError = URLEncoder.encode("삭제할 사용자를 선택하세요.", StandardCharsets.UTF_8);
                return "redirect:/admin/users?error=" + encodedError;
            }
            userRepository.deleteAllByIdInBatch(ids);
            logger.debug("사용자 다중 삭제 완료: count={}", ids.size());
            String encodedMessage = URLEncoder.encode("선택한 사용자가 삭제되었습니다.", StandardCharsets.UTF_8);
            return "redirect:/admin/users?message=" + encodedMessage;
        } catch (Exception e) {
            logger.error("사용자 다중 삭제 실패: error={}", e.getMessage());
            String encodedError = URLEncoder.encode("사용자 삭제 중 오류가 발생했습니다.", StandardCharsets.UTF_8);
            return "redirect:/admin/users?error=" + encodedError;
        }
    }

    @PostMapping("/admin/users/{id}/delete")
    public String deleteUser(@PathVariable Long id) {
        logger.debug("사용자 개별 삭제 요청: id={}", id);
        try {
            if (!userRepository.existsById(id)) {
                logger.warn("삭제 대상 사용자 없음: id={}", id);
                String encodedError = URLEncoder.encode("존재하지 않는 사용자입니다.", StandardCharsets.UTF_8);
                return "redirect:/admin/users?error=" + encodedError;
            }
            userRepository.deleteById(id);
            logger.debug("사용자 개별 삭제 완료: id={}", id);
            String encodedMessage = URLEncoder.encode("사용자가 삭제되었습니다.", StandardCharsets.UTF_8);
            return "redirect:/admin/users?message=" + encodedMessage;
        } catch (Exception e) {
            logger.error("사용자 개별 삭제 실패: id={}, error={}", id, e.getMessage());
            String encodedError = URLEncoder.encode("사용자 삭제 중 오류가 발생했습니다.", StandardCharsets.UTF_8);
            return "redirect:/admin/users?error=" + encodedError;
        }
    }
}