package com.matchhub.catconnect.domain.inbox.repository;

import com.matchhub.catconnect.domain.inbox.model.entity.InboxItem;
import com.matchhub.catconnect.domain.inbox.model.enums.InboxItemType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InboxItemRepository extends JpaRepository<InboxItem, Long> {

    @Query("SELECT COUNT(i) FROM InboxItem i " +
            "WHERE i.recipient.id = :recipientId AND i.readAt IS NULL AND i.deletedAt IS NULL")
    long countUnread(@Param("recipientId") Long recipientId);

    @Query("SELECT i FROM InboxItem i " +
            "LEFT JOIN FETCH i.sender " +
            "WHERE i.recipient.id = :recipientId AND i.deletedAt IS NULL " +
            "ORDER BY i.pinnedAt DESC NULLS LAST, i.updatedDttm DESC")
    List<InboxItem> findByRecipientId(@Param("recipientId") Long recipientId, Pageable pageable);

    @Query("SELECT i FROM InboxItem i " +
            "LEFT JOIN FETCH i.sender " +
            "WHERE i.recipient.id = :recipientId AND i.itemType = :itemType AND i.deletedAt IS NULL " +
            "ORDER BY i.pinnedAt DESC NULLS LAST, i.updatedDttm DESC")
    List<InboxItem> findByRecipientIdAndType(@Param("recipientId") Long recipientId,
                                             @Param("itemType") InboxItemType itemType,
                                             Pageable pageable);

    @Query("SELECT i FROM InboxItem i " +
            "WHERE i.recipient.id = :recipientId AND i.itemType = 'CHAT' " +
            "AND i.referenceId = :referenceId AND i.deletedAt IS NULL")
    Optional<InboxItem> findChatInboxItem(@Param("recipientId") Long recipientId,
                                          @Param("referenceId") Long referenceId);

    @Modifying
    @Query("UPDATE InboxItem i SET i.readAt = CURRENT_TIMESTAMP " +
            "WHERE i.recipient.id = :recipientId AND i.readAt IS NULL AND i.deletedAt IS NULL")
    void markAllAsRead(@Param("recipientId") Long recipientId);

    @Query("SELECT COUNT(i) FROM InboxItem i " +
            "WHERE i.recipient.id = :recipientId AND i.pinnedAt IS NOT NULL AND i.deletedAt IS NULL")
    long countPinned(@Param("recipientId") Long recipientId);
}
