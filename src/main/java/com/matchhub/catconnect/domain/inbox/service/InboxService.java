package com.matchhub.catconnect.domain.inbox.service;

import com.matchhub.catconnect.domain.inbox.model.dto.InboxItemResponseDTO;
import com.matchhub.catconnect.domain.inbox.model.entity.InboxItem;
import com.matchhub.catconnect.domain.inbox.model.enums.InboxItemType;
import com.matchhub.catconnect.domain.inbox.repository.InboxItemRepository;
import com.matchhub.catconnect.domain.user.model.entity.User;
import com.matchhub.catconnect.domain.user.repository.UserRepository;
import com.matchhub.catconnect.global.exception.AppException;
import com.matchhub.catconnect.global.exception.Domain;
import com.matchhub.catconnect.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InboxService {

    private static final int MAX_PIN_COUNT = 5;

    private final InboxItemRepository inboxItemRepository;
    private final UserRepository userRepository;

    public long getUnreadCount(String username) {
        User user = findUserByUsername(username);
        return inboxItemRepository.countUnread(user.getId());
    }

    public List<InboxItemResponseDTO> getInboxItems(String username, InboxItemType type, int page, int size) {
        User user = findUserByUsername(username);
        PageRequest pageable = PageRequest.of(page, size);

        List<InboxItem> items;
        if (type != null) {
            items = inboxItemRepository.findByRecipientIdAndType(user.getId(), type, pageable);
        } else {
            items = inboxItemRepository.findByRecipientId(user.getId(), pageable);
        }

        return items.stream().map(InboxItemResponseDTO::from).toList();
    }

    @Transactional
    public void markAsRead(String username, Long itemId) {
        InboxItem item = findInboxItemForUser(username, itemId);
        item.markAsRead();
    }

    @Transactional
    public void markAllAsRead(String username) {
        User user = findUserByUsername(username);
        inboxItemRepository.markAllAsRead(user.getId());
    }

    @Transactional
    public void pinItem(String username, Long itemId) {
        User user = findUserByUsername(username);
        long pinnedCount = inboxItemRepository.countPinned(user.getId());
        if (pinnedCount >= MAX_PIN_COUNT) {
            throw new AppException(Domain.NONE, ErrorCode.INVALID_REQUEST, "최대 " + MAX_PIN_COUNT + "개까지 고정할 수 있습니다.");
        }
        InboxItem item = findInboxItemForUser(username, itemId);
        item.pin();
    }

    @Transactional
    public void unpinItem(String username, Long itemId) {
        InboxItem item = findInboxItemForUser(username, itemId);
        item.unpin();
    }

    @Transactional
    public void deleteItem(String username, Long itemId) {
        InboxItem item = findInboxItemForUser(username, itemId);
        item.softDelete();
    }

    @Transactional
    public void createOrUpdateChatInboxItem(User recipient, User sender, Long chatRoomId, String preview) {
        var existing = inboxItemRepository.findChatInboxItem(recipient.getId(), chatRoomId);

        if (existing.isPresent()) {
            InboxItem item = existing.get();
            String truncatedPreview = preview != null && preview.length() > 200
                    ? preview.substring(0, 200) : preview;
            item.updatePreview(truncatedPreview, sender);
        } else {
            InboxItem item = InboxItem.builder()
                    .recipient(recipient)
                    .itemType(InboxItemType.CHAT)
                    .referenceId(chatRoomId)
                    .title(sender.getUsername())
                    .preview(preview != null && preview.length() > 200 ? preview.substring(0, 200) : preview)
                    .linkUrl("/chat/" + chatRoomId)
                    .sender(sender)
                    .build();
            inboxItemRepository.save(item);
        }
    }

    @Transactional
    public void markChatAsRead(String username, Long chatRoomId) {
        User user = findUserByUsername(username);
        var existing = inboxItemRepository.findChatInboxItem(user.getId(), chatRoomId);
        existing.ifPresent(InboxItem::markAsRead);
    }

    private InboxItem findInboxItemForUser(String username, Long itemId) {
        User user = findUserByUsername(username);
        InboxItem item = inboxItemRepository.findById(itemId)
                .orElseThrow(() -> new AppException(Domain.NONE, ErrorCode.INVALID_REQUEST, "인박스 항목을 찾을 수 없습니다."));
        if (!item.getRecipient().getId().equals(user.getId())) {
            throw new AppException(Domain.NONE, ErrorCode.ACCESS_DENIED);
        }
        return item;
    }

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(Domain.USER, ErrorCode.USER_NOT_FOUND));
    }
}
