package com.matchhub.catconnect.domain.profile.controller;

import com.matchhub.catconnect.domain.board.repository.BoardRepository;
import com.matchhub.catconnect.domain.comment.repository.CommentRepository;
import com.matchhub.catconnect.domain.file.model.dto.FileResponseDTO;
import com.matchhub.catconnect.domain.profile.service.ProfileService;
import com.matchhub.catconnect.domain.user.model.dto.UserResponseDTO;
import com.matchhub.catconnect.domain.user.model.entity.User;
import com.matchhub.catconnect.domain.user.repository.UserRepository;
import com.matchhub.catconnect.domain.user.service.UserService;
import com.matchhub.catconnect.global.exception.AppException;
import com.matchhub.catconnect.global.exception.Domain;
import com.matchhub.catconnect.global.exception.ErrorCode;
import com.matchhub.catconnect.global.exception.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 프로필 관련 REST API
 */
@Tag(name = "프로필 API", description = "사용자 프로필 관련 REST API")
@RestController
@RequestMapping("/api/profile")
public class ProfileRestController {

    private static final Logger log = LoggerFactory.getLogger(ProfileRestController.class);

    private final ProfileService profileService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;

    public ProfileRestController(ProfileService profileService, UserService userService,
                                 UserRepository userRepository, PasswordEncoder passwordEncoder,
                                 BoardRepository boardRepository, CommentRepository commentRepository) {
        this.profileService = profileService;
        this.userService = userService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.boardRepository = boardRepository;
        this.commentRepository = commentRepository;
    }

    @Getter
    @Setter
    public static class ProfileUpdateRequestDTO {
        private String email;
        private String phoneNumber;
        private String currentPassword;
        private String newPassword;
    }

    @Getter
    @Setter
    public static class AccountDeleteRequestDTO {
        private String password;
    }

    @Operation(summary = "비밀번호 확인", description = "현재 비밀번호를 확인합니다.")
    @PostMapping("/verify-password")
    public ResponseEntity<Response<?>> verifyPassword(
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        String username = authentication.getName();
        String password = request.get("password");

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(Domain.USER, ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Response.error("비밀번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED));
        }

        return ResponseEntity.ok(Response.success(null, "비밀번호 확인 성공"));
    }

    @Operation(summary = "내 프로필 조회", description = "현재 로그인한 사용자의 프로필 정보를 조회합니다.")
    @GetMapping
    public ResponseEntity<Response<UserResponseDTO>> getMyProfile(Authentication authentication) {
        String username = authentication.getName();
        log.debug("GET /api/profile 요청: username={}", username);

        UserResponseDTO user = userService.getUserByUsername(username);
        return ResponseEntity.ok(Response.success(user, "프로필 조회 성공"));
    }

    @Operation(summary = "프로필 정보 수정", description = "현재 로그인한 사용자의 프로필 정보를 수정합니다.")
    @PutMapping
    @Transactional
    public ResponseEntity<Response<Void>> updateProfile(
            Authentication authentication,
            @RequestBody ProfileUpdateRequestDTO requestDTO) {
        String username = authentication.getName();
        log.debug("PUT /api/profile 요청: username={}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(Domain.USER, ErrorCode.USER_NOT_FOUND));

        String newPassword = user.getPassword();
        String newEmail = user.getEmail();
        String newPhoneNumber = user.getPhoneNumber();

        // 비밀번호 변경 처리
        if (requestDTO.getNewPassword() != null && !requestDTO.getNewPassword().isBlank()) {
            if (requestDTO.getCurrentPassword() == null || requestDTO.getCurrentPassword().isBlank()) {
                throw new AppException(Domain.USER, ErrorCode.INVALID_REQUEST, "비밀번호 변경 시 현재 비밀번호를 입력해야 합니다.");
            }
            if (!passwordEncoder.matches(requestDTO.getCurrentPassword(), user.getPassword())) {
                throw new AppException(Domain.USER, ErrorCode.USER_INVALID_CREDENTIALS, "현재 비밀번호가 일치하지 않습니다.");
            }
            newPassword = passwordEncoder.encode(requestDTO.getNewPassword());
        }

        // 이메일 변경 처리
        if (requestDTO.getEmail() != null && !requestDTO.getEmail().isBlank()) {
            if (!requestDTO.getEmail().equals(user.getEmail()) && userRepository.existsByEmail(requestDTO.getEmail())) {
                throw new AppException(Domain.USER, ErrorCode.USER_DUPLICATE_EMAIL);
            }
            newEmail = requestDTO.getEmail();
        }

        // 전화번호 변경 처리
        if (requestDTO.getPhoneNumber() != null) {
            newPhoneNumber = requestDTO.getPhoneNumber();
        }

        user.update(user.getUsername(), newEmail, newPhoneNumber, newPassword);
        userRepository.save(user);

        return ResponseEntity.ok(Response.success(null, "프로필 수정 성공"));
    }

    @Operation(summary = "회원 탈퇴", description = "현재 로그인한 사용자의 계정을 탈퇴(소프트 삭제) 처리합니다.")
    @DeleteMapping
    @Transactional
    public ResponseEntity<Response<Void>> deleteAccount(
            Authentication authentication,
            @RequestBody AccountDeleteRequestDTO requestDTO) {
        String username = authentication.getName();
        log.debug("DELETE /api/profile 요청: username={}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(Domain.USER, ErrorCode.USER_NOT_FOUND));

        // 비밀번호 검증
        if (requestDTO.getPassword() == null || !passwordEncoder.matches(requestDTO.getPassword(), user.getPassword())) {
            throw new AppException(Domain.USER, ErrorCode.USER_INVALID_CREDENTIALS, "비밀번호가 일치하지 않습니다.");
        }

        // 소프트 삭제
        user.softDelete();
        userRepository.save(user);

        // 게시글/댓글 작성자명 변경
        boardRepository.updateAuthorByAuthor(username, "(탈퇴한 사용자)");
        commentRepository.updateAuthorByAuthor(username, "(탈퇴한 사용자)");

        return ResponseEntity.ok(Response.success(null, "회원 탈퇴 성공"));
    }

    @Operation(summary = "프로필 이미지 업로드", description = "프로필 이미지를 업로드합니다.")
    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Response<FileResponseDTO>> uploadProfileImage(
            Authentication authentication,
            @RequestParam("file") MultipartFile file) {
        String username = authentication.getName();
        log.debug("POST /api/profile/image 요청: username={}", username);

        FileResponseDTO fileResponse = profileService.uploadProfileImage(username, file);
        return ResponseEntity.ok(Response.success(fileResponse, "프로필 이미지 업로드 성공"));
    }

    @Operation(summary = "프로필 이미지 삭제", description = "프로필 이미지를 삭제합니다.")
    @DeleteMapping("/image")
    public ResponseEntity<Response<Void>> deleteProfileImage(Authentication authentication) {
        String username = authentication.getName();
        log.debug("DELETE /api/profile/image 요청: username={}", username);

        profileService.deleteProfileImage(username);
        return ResponseEntity.ok(Response.success(null, "프로필 이미지 삭제 성공"));
    }

    @Operation(summary = "프로필 이미지 URL 조회", description = "프로필 이미지 URL만 조회합니다.")
    @GetMapping("/image")
    public ResponseEntity<Response<Map<String, String>>> getProfileImageUrl(Authentication authentication) {
        String username = authentication.getName();
        log.debug("GET /api/profile/image 요청: username={}", username);

        String profileImageUrl = profileService.getProfileImageUrl(username);
        return ResponseEntity.ok(Response.success(
                Map.of("profileImageUrl", profileImageUrl != null ? profileImageUrl : ""),
                "프로필 이미지 URL 조회 성공"));
    }
}
