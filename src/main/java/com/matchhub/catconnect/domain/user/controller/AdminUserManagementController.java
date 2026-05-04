package com.matchhub.catconnect.domain.user.controller;

import com.matchhub.catconnect.domain.user.model.entity.User;
import com.matchhub.catconnect.domain.user.model.enums.Role;
import com.matchhub.catconnect.domain.user.repository.UserRepository;
import com.matchhub.catconnect.global.exception.AppException;
import com.matchhub.catconnect.global.exception.Domain;
import com.matchhub.catconnect.global.exception.ErrorCode;
import com.matchhub.catconnect.global.exception.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserManagementController {

    private final UserRepository userRepository;

    // 권한 변경 (ADMIN만)
    @PutMapping("/{userId}/role")
    public ResponseEntity<Response<Void>> changeRole(@PathVariable Long userId, @RequestBody Map<String, String> request) {
        User user = findUser(userId);
        if (user.getRole() == Role.ADMIN) {
            throw new AppException(Domain.USER, ErrorCode.INVALID_REQUEST, "관리자 권한은 변경할 수 없습니다.");
        }
        Role newRole = Role.valueOf(request.get("role"));
        if (newRole == Role.ADMIN) {
            throw new AppException(Domain.USER, ErrorCode.INVALID_REQUEST, "관리자 권한으로 변경할 수 없습니다.");
        }
        user.changeRole(newRole);
        userRepository.save(user);
        return ResponseEntity.ok(Response.success(null, "권한이 변경되었습니다."));
    }

    // 활동정지 (ADMIN: 무제한, MANAGER: 7일 이하는 /api/manager/에서 처리)
    @PostMapping("/{userId}/suspend")
    public ResponseEntity<Response<Void>> suspendUser(@PathVariable Long userId, @RequestBody Map<String, Object> request) {
        User user = findUser(userId);
        if (user.getRole() == Role.ADMIN) {
            throw new AppException(Domain.USER, ErrorCode.INVALID_REQUEST, "관리자를 정지할 수 없습니다.");
        }
        int days = ((Number) request.get("days")).intValue();
        String reason = (String) request.getOrDefault("reason", "관리자에 의한 활동정지");
        LocalDateTime until = days <= 0 ? LocalDateTime.of(9999, 12, 31, 23, 59) : LocalDateTime.now().plusDays(days);
        user.suspend(until, reason);
        userRepository.save(user);
        return ResponseEntity.ok(Response.success(null, days <= 0 ? "영구 정지되었습니다." : days + "일 활동정지 처리되었습니다."));
    }

    // 활동정지 해제
    @PostMapping("/{userId}/unsuspend")
    public ResponseEntity<Response<Void>> unsuspendUser(@PathVariable Long userId) {
        User user = findUser(userId);
        user.unsuspend();
        userRepository.save(user);
        return ResponseEntity.ok(Response.success(null, "정지가 해제되었습니다."));
    }

    // 강제탈퇴
    @PostMapping("/{userId}/force-delete")
    public ResponseEntity<Response<Void>> forceDeleteUser(@PathVariable Long userId) {
        User user = findUser(userId);
        if (user.getRole() == Role.ADMIN) {
            throw new AppException(Domain.USER, ErrorCode.INVALID_REQUEST, "관리자를 강제탈퇴할 수 없습니다.");
        }
        user.softDelete();
        userRepository.save(user);
        return ResponseEntity.ok(Response.success(null, "강제탈퇴 처리되었습니다."));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new AppException(Domain.USER, ErrorCode.USER_NOT_FOUND));
    }
}
