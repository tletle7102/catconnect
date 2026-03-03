package com.matchhub.catconnect.domain.profile.controller;

import com.matchhub.catconnect.domain.file.model.dto.FileResponseDTO;
import com.matchhub.catconnect.domain.profile.service.ProfileService;
import com.matchhub.catconnect.domain.user.model.dto.UserResponseDTO;
import com.matchhub.catconnect.domain.user.service.UserService;
import com.matchhub.catconnect.global.exception.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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

    public ProfileRestController(ProfileService profileService, UserService userService) {
        this.profileService = profileService;
        this.userService = userService;
    }

    @Operation(summary = "내 프로필 조회", description = "현재 로그인한 사용자의 프로필 정보를 조회합니다.")
    @GetMapping
    public ResponseEntity<Response<UserResponseDTO>> getMyProfile(Authentication authentication) {
        String username = authentication.getName();
        log.debug("GET /api/profile 요청: username={}", username);

        UserResponseDTO user = userService.getUserByUsername(username);
        return ResponseEntity.ok(Response.success(user, "프로필 조회 성공"));
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
