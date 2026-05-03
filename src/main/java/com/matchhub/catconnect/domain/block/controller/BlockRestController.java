package com.matchhub.catconnect.domain.block.controller;

import com.matchhub.catconnect.domain.block.model.dto.BlockResponseDTO;
import com.matchhub.catconnect.domain.block.service.BlockService;
import com.matchhub.catconnect.global.exception.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/blocks")
@RequiredArgsConstructor
public class BlockRestController {

    private final BlockService blockService;

    @PostMapping
    public ResponseEntity<Response<Void>> blockUser(
            @RequestBody Map<String, Long> request,
            Authentication authentication) {
        String username = authentication.getName();
        blockService.blockUser(username, request.get("targetUserId"));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Response.success(null, "차단되었습니다."));
    }

    @DeleteMapping("/{targetUserId}")
    public ResponseEntity<Response<Void>> unblockUser(
            @PathVariable Long targetUserId,
            Authentication authentication) {
        String username = authentication.getName();
        blockService.unblockUser(username, targetUserId);
        return ResponseEntity.ok(Response.success(null, "차단이 해제되었습니다."));
    }

    @GetMapping
    public ResponseEntity<Response<List<BlockResponseDTO>>> getBlockList(Authentication authentication) {
        String username = authentication.getName();
        List<BlockResponseDTO> blocks = blockService.getBlockList(username);
        return ResponseEntity.ok(Response.success(blocks));
    }

    @GetMapping("/check/{targetUserId}")
    public ResponseEntity<Response<Map<String, Boolean>>> checkBlocked(
            @PathVariable Long targetUserId,
            Authentication authentication) {
        String username = authentication.getName();
        boolean blocked = blockService.isBlocked(username, targetUserId);
        return ResponseEntity.ok(Response.success(Map.of("blocked", blocked)));
    }
}
