package com.matchhub.catconnect.domain.inbox.controller;

import com.matchhub.catconnect.domain.inbox.model.dto.InboxItemResponseDTO;
import com.matchhub.catconnect.domain.inbox.model.dto.UnreadCountDTO;
import com.matchhub.catconnect.domain.inbox.model.enums.InboxItemType;
import com.matchhub.catconnect.domain.inbox.service.InboxService;
import com.matchhub.catconnect.global.exception.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inbox")
@RequiredArgsConstructor
public class InboxRestController {

    private final InboxService inboxService;

    @GetMapping
    public ResponseEntity<Response<List<InboxItemResponseDTO>>> getInboxItems(
            @RequestParam(required = false) InboxItemType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        String username = authentication.getName();
        List<InboxItemResponseDTO> items = inboxService.getInboxItems(username, type, page, size);
        return ResponseEntity.ok(Response.success(items));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Response<UnreadCountDTO>> getUnreadCount(Authentication authentication) {
        String username = authentication.getName();
        long count = inboxService.getUnreadCount(username);
        return ResponseEntity.ok(Response.success(new UnreadCountDTO(count)));
    }

    @PostMapping("/{itemId}/read")
    public ResponseEntity<Response<Void>> markAsRead(
            @PathVariable Long itemId,
            Authentication authentication) {
        String username = authentication.getName();
        inboxService.markAsRead(username, itemId);
        return ResponseEntity.ok(Response.success(null, "읽음 처리되었습니다."));
    }

    @PostMapping("/read-all")
    public ResponseEntity<Response<Void>> markAllAsRead(Authentication authentication) {
        String username = authentication.getName();
        inboxService.markAllAsRead(username);
        return ResponseEntity.ok(Response.success(null, "모두 읽음 처리되었습니다."));
    }

    @PostMapping("/{itemId}/pin")
    public ResponseEntity<Response<Void>> pinItem(
            @PathVariable Long itemId,
            Authentication authentication) {
        String username = authentication.getName();
        inboxService.pinItem(username, itemId);
        return ResponseEntity.ok(Response.success(null, "고정되었습니다."));
    }

    @DeleteMapping("/{itemId}/pin")
    public ResponseEntity<Response<Void>> unpinItem(
            @PathVariable Long itemId,
            Authentication authentication) {
        String username = authentication.getName();
        inboxService.unpinItem(username, itemId);
        return ResponseEntity.ok(Response.success(null, "고정이 해제되었습니다."));
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Response<Void>> deleteItem(
            @PathVariable Long itemId,
            Authentication authentication) {
        String username = authentication.getName();
        inboxService.deleteItem(username, itemId);
        return ResponseEntity.ok(Response.success(null, "삭제되었습니다."));
    }
}
