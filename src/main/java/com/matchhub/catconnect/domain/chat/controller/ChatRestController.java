package com.matchhub.catconnect.domain.chat.controller;

import com.matchhub.catconnect.domain.chat.model.dto.ChatHistoryResponseDTO;
import com.matchhub.catconnect.domain.chat.model.dto.ChatRoomResponseDTO;
import com.matchhub.catconnect.domain.chat.model.dto.CreateRoomRequestDTO;
import com.matchhub.catconnect.domain.chat.service.ChatMessageService;
import com.matchhub.catconnect.domain.chat.service.ChatRoomService;
import com.matchhub.catconnect.global.exception.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatRestController {

    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;

    @PostMapping("/rooms")
    public ResponseEntity<Response<ChatRoomResponseDTO>> createOrGetRoom(
            @Valid @RequestBody CreateRoomRequestDTO request,
            Authentication authentication) {
        String username = authentication.getName();
        ChatRoomResponseDTO room = chatRoomService.createOrGetRoom(username, request.getTargetUserId(), request.getRoomType());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Response.success(room, "채팅방이 생성되었습니다."));
    }

    @GetMapping("/rooms")
    public ResponseEntity<Response<List<ChatRoomResponseDTO>>> getRoomList(Authentication authentication) {
        String username = authentication.getName();
        List<ChatRoomResponseDTO> rooms = chatRoomService.getRoomList(username);
        return ResponseEntity.ok(Response.success(rooms));
    }

    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<Response<ChatHistoryResponseDTO>> getMessages(
            @PathVariable Long roomId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "50") int size,
            Authentication authentication) {
        String username = authentication.getName();
        ChatHistoryResponseDTO history = chatMessageService.getHistory(username, roomId, cursor, size);
        return ResponseEntity.ok(Response.success(history));
    }

    @PostMapping("/rooms/{roomId}/read")
    public ResponseEntity<Response<Void>> markAsRead(
            @PathVariable Long roomId,
            @RequestParam Long lastReadMessageId,
            Authentication authentication) {
        String username = authentication.getName();
        chatMessageService.markAsRead(username, roomId, lastReadMessageId);
        return ResponseEntity.ok(Response.success(null, "읽음 처리되었습니다."));
    }

    @PostMapping("/rooms/{roomId}/leave")
    public ResponseEntity<Response<Void>> leaveRoom(
            @PathVariable Long roomId,
            Authentication authentication) {
        String username = authentication.getName();
        chatRoomService.leaveRoom(username, roomId);
        return ResponseEntity.ok(Response.success(null, "채팅방을 나갔습니다."));
    }
}
