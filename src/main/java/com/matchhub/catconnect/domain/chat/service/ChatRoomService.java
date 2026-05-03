package com.matchhub.catconnect.domain.chat.service;

import com.matchhub.catconnect.domain.block.service.BlockService;
import com.matchhub.catconnect.domain.chat.model.dto.ChatRoomResponseDTO;
import com.matchhub.catconnect.domain.chat.model.entity.ChatMessage;
import com.matchhub.catconnect.domain.chat.model.entity.ChatRoom;
import com.matchhub.catconnect.domain.chat.model.entity.ChatRoomParticipant;
import com.matchhub.catconnect.domain.chat.model.enums.RoomType;
import com.matchhub.catconnect.domain.chat.repository.ChatMessageRepository;
import com.matchhub.catconnect.domain.chat.repository.ChatRoomParticipantRepository;
import com.matchhub.catconnect.domain.chat.repository.ChatRoomRepository;
import com.matchhub.catconnect.domain.user.model.entity.User;
import com.matchhub.catconnect.domain.user.repository.UserRepository;
import com.matchhub.catconnect.global.exception.AppException;
import com.matchhub.catconnect.global.exception.Domain;
import com.matchhub.catconnect.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomParticipantRepository participantRepository;
    private final ChatMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final BlockService blockService;

    @Transactional
    public ChatRoomResponseDTO createOrGetRoom(String username, Long targetUserId, RoomType roomType) {
        User currentUser = findUserByUsername(username);
        User targetUser = findUserById(targetUserId);

        if (currentUser.getId().equals(targetUser.getId())) {
            throw new AppException(Domain.NONE, ErrorCode.INVALID_REQUEST, "자기 자신과 채팅할 수 없습니다.");
        }

        if (targetUser.isDeleted()) {
            throw new AppException(Domain.USER, ErrorCode.INVALID_REQUEST, "탈퇴한 사용자와 대화할 수 없습니다.");
        }

        if (blockService.isBlockedBetween(currentUser.getId(), targetUser.getId())) {
            throw new AppException(Domain.NONE, ErrorCode.INVALID_REQUEST, "대화할 수 없는 사용자입니다.");
        }

        if (roomType == RoomType.SUPPORT) {
            return getOrCreateSupportRoom(currentUser, targetUser);
        }

        return getOrCreateDirectRoom(currentUser, targetUser);
    }

    private ChatRoomResponseDTO getOrCreateDirectRoom(User user1, User user2) {
        Long smallerId = Math.min(user1.getId(), user2.getId());
        Long largerId = Math.max(user1.getId(), user2.getId());

        var existingRoom = chatRoomRepository.findDirectRoom(RoomType.DIRECT, smallerId, largerId);

        if (existingRoom.isPresent()) {
            ChatRoom room = existingRoom.get();
            // 나갔던 유저가 다시 진입하면 rejoin
            var participant = participantRepository.findByRoomIdAndUserId(room.getId(), user1.getId());
            participant.ifPresent(p -> {
                if (!p.isActive()) p.rejoin();
            });
            return buildRoomResponse(room, user1);
        }

        ChatRoom room = new ChatRoom(RoomType.DIRECT);
        chatRoomRepository.save(room);

        ChatRoomParticipant p1 = new ChatRoomParticipant(room, user1);
        ChatRoomParticipant p2 = new ChatRoomParticipant(room, user2);
        participantRepository.save(p1);
        participantRepository.save(p2);

        // 시스템 메시지
        ChatMessage systemMsg = ChatMessage.systemMessage(room, "대화가 시작되었습니다.");
        messageRepository.save(systemMsg);

        return buildRoomResponse(room, user1);
    }

    private ChatRoomResponseDTO getOrCreateSupportRoom(User user, User admin) {
        var existingRoom = chatRoomRepository.findSupportRoomByUserId(user.getId());

        if (existingRoom.isPresent()) {
            return buildRoomResponse(existingRoom.get(), user);
        }

        ChatRoom room = new ChatRoom(RoomType.SUPPORT);
        chatRoomRepository.save(room);

        ChatRoomParticipant p1 = new ChatRoomParticipant(room, user);
        ChatRoomParticipant p2 = new ChatRoomParticipant(room, admin);
        participantRepository.save(p1);
        participantRepository.save(p2);

        ChatMessage systemMsg = ChatMessage.systemMessage(room, "고객지원 대화가 시작되었습니다.");
        messageRepository.save(systemMsg);

        return buildRoomResponse(room, user);
    }

    public List<ChatRoomResponseDTO> getRoomList(String username) {
        User user = findUserByUsername(username);
        List<ChatRoomParticipant> myParticipants = participantRepository.findActiveByUserId(user.getId());

        List<ChatRoomResponseDTO> result = new ArrayList<>();
        for (ChatRoomParticipant myPart : myParticipants) {
            ChatRoom room = myPart.getChatRoom();
            result.add(buildRoomResponseWithUnread(room, user, myPart));
        }
        return result;
    }

    @Transactional
    public void leaveRoom(String username, Long roomId) {
        User user = findUserByUsername(username);
        ChatRoomParticipant participant = participantRepository.findByRoomIdAndUserId(roomId, user.getId())
                .orElseThrow(() -> new AppException(Domain.NONE, ErrorCode.INVALID_REQUEST, "채팅방 참여자가 아닙니다."));

        participant.leave();

        // 시스템 메시지
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new AppException(Domain.NONE, ErrorCode.INVALID_REQUEST));
        ChatMessage systemMsg = ChatMessage.systemMessage(room, user.getUsername() + "님이 나갔습니다.");
        messageRepository.save(systemMsg);
    }

    public void validateParticipant(Long roomId, String username) {
        User user = findUserByUsername(username);
        ChatRoomParticipant participant = participantRepository.findByRoomIdAndUserId(roomId, user.getId())
                .orElseThrow(() -> new AppException(Domain.NONE, ErrorCode.ACCESS_DENIED, "채팅방 참여자가 아닙니다."));
        if (!participant.isActive()) {
            throw new AppException(Domain.NONE, ErrorCode.ACCESS_DENIED, "나간 채팅방입니다.");
        }
    }

    private ChatRoomResponseDTO buildRoomResponse(ChatRoom room, User currentUser) {
        var otherParticipants = participantRepository.findOtherParticipants(room.getId(), currentUser.getId());
        var latestMessages = messageRepository.findTopByRoomId(room.getId(), PageRequest.of(0, 1));

        ChatRoomResponseDTO.OtherUserDTO otherUserDTO = otherParticipants.isEmpty() ? null : buildOtherUserDTO(otherParticipants.get(0));

        String lastMessage = latestMessages.isEmpty() ? null : latestMessages.get(0).getContent();

        return ChatRoomResponseDTO.builder()
                .roomId(room.getId())
                .roomType(room.getRoomType())
                .otherUser(otherUserDTO)
                .lastMessage(lastMessage)
                .unreadCount(0L)
                .updatedAt(room.getUpdatedDttm())
                .build();
    }

    private ChatRoomResponseDTO buildRoomResponseWithUnread(ChatRoom room, User currentUser, ChatRoomParticipant myPart) {
        var otherParticipants = participantRepository.findOtherParticipants(room.getId(), currentUser.getId());
        var latestMessages = messageRepository.findTopByRoomId(room.getId(), PageRequest.of(0, 1));
        long unreadCount = messageRepository.countUnreadMessages(room.getId(), myPart.getLastReadMessageId());

        ChatRoomResponseDTO.OtherUserDTO otherUserDTO = otherParticipants.isEmpty() ? null : buildOtherUserDTO(otherParticipants.get(0));

        String lastMessage = latestMessages.isEmpty() ? null : latestMessages.get(0).getContent();

        return ChatRoomResponseDTO.builder()
                .roomId(room.getId())
                .roomType(room.getRoomType())
                .otherUser(otherUserDTO)
                .lastMessage(lastMessage)
                .unreadCount(unreadCount)
                .updatedAt(room.getUpdatedDttm())
                .build();
    }

    private ChatRoomResponseDTO.OtherUserDTO buildOtherUserDTO(ChatRoomParticipant participant) {
        User other = participant.getUser();
        return ChatRoomResponseDTO.OtherUserDTO.builder()
                .id(other.getId())
                .username(other.isDeleted() ? "탈퇴한 사용자" : other.getUsername())
                .profileImageUrl(other.getProfileImageUrl())
                .deleted(other.isDeleted())
                .build();
    }

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(Domain.USER, ErrorCode.USER_NOT_FOUND));
    }

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new AppException(Domain.USER, ErrorCode.USER_NOT_FOUND));
    }
}
