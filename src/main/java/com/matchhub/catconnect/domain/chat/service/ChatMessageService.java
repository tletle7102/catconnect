package com.matchhub.catconnect.domain.chat.service;

import com.matchhub.catconnect.domain.block.service.BlockService;
import com.matchhub.catconnect.domain.chat.model.dto.ChatHistoryResponseDTO;
import com.matchhub.catconnect.domain.chat.model.dto.ChatMessageResponseDTO;
import com.matchhub.catconnect.domain.chat.model.dto.SendMessageDTO;
import com.matchhub.catconnect.domain.chat.model.entity.ChatMessage;
import com.matchhub.catconnect.domain.chat.model.entity.ChatRoom;
import com.matchhub.catconnect.domain.chat.model.entity.ChatRoomParticipant;
import com.matchhub.catconnect.domain.chat.model.enums.MessageType;
import com.matchhub.catconnect.domain.chat.repository.ChatMessageRepository;
import com.matchhub.catconnect.domain.chat.repository.ChatRoomParticipantRepository;
import com.matchhub.catconnect.domain.chat.repository.ChatRoomRepository;
import com.matchhub.catconnect.domain.file.model.entity.FileEntity;
import com.matchhub.catconnect.domain.file.repository.FileRepository;
import com.matchhub.catconnect.domain.inbox.service.InboxService;
import com.matchhub.catconnect.domain.notification.sse.SseEmitterService;
import com.matchhub.catconnect.domain.user.model.entity.User;
import com.matchhub.catconnect.domain.user.repository.UserRepository;
import com.matchhub.catconnect.global.exception.AppException;
import com.matchhub.catconnect.global.exception.Domain;
import com.matchhub.catconnect.global.exception.ErrorCode;
import com.matchhub.catconnect.global.util.HtmlSanitizer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatMessageService {

    private static final int DEFAULT_PAGE_SIZE = 50;

    private final ChatMessageRepository messageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomParticipantRepository participantRepository;
    private final UserRepository userRepository;
    private final BlockService blockService;
    private final InboxService inboxService;
    private final SseEmitterService sseEmitterService;
    private final FileRepository fileRepository;
    private final HtmlSanitizer htmlSanitizer;

    @Transactional
    public ChatMessageResponseDTO sendMessage(String senderUsername, SendMessageDTO dto) {
        User sender = findUserByUsername(senderUsername);
        ChatRoom room = chatRoomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new AppException(Domain.NONE, ErrorCode.INVALID_REQUEST, "채팅방을 찾을 수 없습니다."));

        // 참여자 검증
        ChatRoomParticipant senderParticipant = participantRepository.findByRoomIdAndUserId(room.getId(), sender.getId())
                .orElseThrow(() -> new AppException(Domain.NONE, ErrorCode.ACCESS_DENIED, "채팅방 참여자가 아닙니다."));

        if (!senderParticipant.isActive()) {
            throw new AppException(Domain.NONE, ErrorCode.ACCESS_DENIED, "나간 채팅방입니다.");
        }

        // 상대방 확인 및 차단/탈퇴 검증
        var otherParticipants = participantRepository.findOtherParticipants(room.getId(), sender.getId());
        if (!otherParticipants.isEmpty()) {
            User other = otherParticipants.get(0).getUser();
            if (other.isDeleted()) {
                throw new AppException(Domain.NONE, ErrorCode.INVALID_REQUEST, "탈퇴한 사용자에게 메시지를 보낼 수 없습니다.");
            }
            if (blockService.isBlockedBetween(sender.getId(), other.getId())) {
                throw new AppException(Domain.NONE, ErrorCode.INVALID_REQUEST, "메시지를 보낼 수 없습니다.");
            }
        }

        // 메시지 내용 새니타이징
        String content = dto.getContent();
        if (content != null && dto.getMessageType() == MessageType.TEXT) {
            content = htmlSanitizer.sanitize(content);
        }

        // 이미지 메시지일 경우 content를 "[사진]"으로 설정 (인박스 미리보기용)
        String previewContent = content;
        if (dto.getMessageType() == MessageType.IMAGE) {
            previewContent = "[사진]";
        }

        ChatMessage message = new ChatMessage(room, sender, content, dto.getMessageType(), dto.getFileId());
        messageRepository.save(message);

        // 파일 URL 조회 (이미지 메시지인 경우)
        String fileUrl = resolveFileUrl(dto.getFileId());

        // 인박스 갱신 + SSE 실시간 알림 (상대방)
        if (!otherParticipants.isEmpty()) {
            User recipient = otherParticipants.get(0).getUser();
            inboxService.createOrUpdateChatInboxItem(recipient, sender, room.getId(), previewContent);

            // SSE로 상대방에게 실시간 알림 push
            long unreadCount = inboxService.getUnreadCount(recipient.getUsername());
            sseEmitterService.pushNotification(recipient.getId(), "chat",
                    Map.of("type", "NEW_CHAT", "senderName", sender.getUsername(),
                            "roomId", room.getId(), "unreadCount", unreadCount));
        }

        return ChatMessageResponseDTO.from(message, fileUrl);
    }

    @Transactional
    public void markAsRead(String username, Long roomId, Long lastReadMessageId) {
        User user = findUserByUsername(username);
        ChatRoomParticipant participant = participantRepository.findByRoomIdAndUserId(roomId, user.getId())
                .orElseThrow(() -> new AppException(Domain.NONE, ErrorCode.ACCESS_DENIED));
        participant.updateLastReadMessageId(lastReadMessageId);

        // 인박스 읽음 처리
        inboxService.markChatAsRead(username, roomId);
    }

    public ChatHistoryResponseDTO getHistory(String username, Long roomId, Long cursor, int size) {
        User user = findUserByUsername(username);

        // 참여자 검증
        participantRepository.findByRoomIdAndUserId(roomId, user.getId())
                .orElseThrow(() -> new AppException(Domain.NONE, ErrorCode.ACCESS_DENIED));

        if (size <= 0 || size > 100) {
            size = DEFAULT_PAGE_SIZE;
        }

        List<ChatMessage> messages;
        if (cursor == null || cursor == 0) {
            messages = messageRepository.findLatestByRoomId(roomId, PageRequest.of(0, size + 1));
        } else {
            messages = messageRepository.findByRoomIdBeforeCursor(roomId, cursor, PageRequest.of(0, size + 1));
        }

        boolean hasMore = messages.size() > size;
        if (hasMore) {
            messages = messages.subList(0, size);
        }

        // 역순 정렬 (오래된 것부터)
        List<ChatMessageResponseDTO> dtos = messages.stream()
                .map(m -> ChatMessageResponseDTO.from(m, resolveFileUrl(m.getFileId())))
                .toList();

        List<ChatMessageResponseDTO> reversed = new java.util.ArrayList<>(dtos);
        Collections.reverse(reversed);

        Long nextCursor = hasMore && !messages.isEmpty() ? messages.get(messages.size() - 1).getId() : null;

        return ChatHistoryResponseDTO.builder()
                .messages(reversed)
                .hasMore(hasMore)
                .nextCursor(nextCursor)
                .build();
    }

    public List<ChatMessageResponseDTO> getMessagesAroundId(Long roomId, Long messageId, int contextSize) {
        Long startId = Math.max(1, messageId - contextSize);
        Long endId = messageId + contextSize;
        return messageRepository.findMessagesInRange(roomId, startId, endId).stream()
                .map(m -> ChatMessageResponseDTO.from(m, resolveFileUrl(m.getFileId())))
                .toList();
    }

    private String resolveFileUrl(Long fileId) {
        if (fileId == null) return null;
        return fileRepository.findById(fileId)
                .map(file -> "/api/files/download/" + file.getStoredName())
                .orElse(null);
    }

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(Domain.USER, ErrorCode.USER_NOT_FOUND));
    }
}
