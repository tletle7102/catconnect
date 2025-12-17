package com.matchhub.catconnect.domain.user.controller;

import com.matchhub.catconnect.domain.user.model.dto.UserRequestDTO;
import com.matchhub.catconnect.domain.user.model.dto.UserResponseDTO;
import com.matchhub.catconnect.domain.user.service.UserService;
import com.matchhub.catconnect.global.exception.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "사용자 API", description = "사용자 관련 REST API")
@RestController
@RequestMapping("/api/users")
public class UserRestController {

    private static final Logger log = LoggerFactory.getLogger(UserRestController.class);
    private final UserService userService;

    // 생성자를 통한 의존성 주입
    public UserRestController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "전체 사용자 조회", description = "모든 사용자 목록을 페이지네이션하여 조회합니다.")
    @GetMapping
    public ResponseEntity<Response<Page<UserResponseDTO>>> getAllUsers(
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size) {
        log.debug("GET /api/users 요청: page={}, size={}", page, size);
        Page<UserResponseDTO> users = userService.getAllUsers(page, size);
        return ResponseEntity.ok(Response.success(users, "사용자 목록 조회 성공"));
    }

    @Operation(summary = "특정 사용자 상세 조회", description = "특정 사용자 정보를 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<Response<UserResponseDTO>> getUserById(@PathVariable Long id) {
        log.debug("GET /api/users/{} 요청", id);
        UserResponseDTO user = userService.getUserById(id);
        return ResponseEntity.ok(Response.success(user, "사용자 조회 성공"));
    }

    @Operation(summary = "사용자 생성", description = "새로운 사용자를 생성합니다.")
    @PostMapping
    public ResponseEntity<Response<UserResponseDTO>> createUser(@Valid @RequestBody UserRequestDTO requestDTO) {
        log.debug("POST /api/users 요청");
        // 서비스 호출하여 사용자 생성
        UserResponseDTO user = userService.createUser(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(Response.success(user, "사용자 생성 성공"));
    }

    @Operation(summary = "사용자 여러 개 삭제", description = "여러 사용자를 삭제합니다.")
    @DeleteMapping
    public ResponseEntity<Response<Void>> deleteUsers(@RequestBody Map<String, List<Long>> request) {
        log.debug("DELETE /api/users 요청");
        // 요청에서 ID 목록 추출
        List<Long> ids = request.get("ids");
        // 서비스 호출하여 사용자 삭제
        userService.deleteUsers(ids);
        return ResponseEntity.ok(Response.success(null, "사용자 삭제 성공"));
    }

    @Operation(summary = "사용자 개별 삭제", description = "특정 사용자를 삭제합니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Response<Void>> deleteUser(
            @Parameter(description = "삭제할 사용자 ID", required = true) @PathVariable Long id) {
        log.debug("DELETE /api/users/{} 요청", id);
        // 서비스 호출하여 사용자 삭제
        userService.deleteUser(id);
        return ResponseEntity.ok(Response.success(null, "사용자 삭제 성공"));
    }
}
