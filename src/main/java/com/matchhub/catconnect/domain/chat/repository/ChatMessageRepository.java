package com.matchhub.catconnect.domain.chat.repository;

import com.matchhub.catconnect.domain.chat.model.entity.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("SELECT m FROM ChatMessage m " +
            "LEFT JOIN FETCH m.sender " +
            "WHERE m.chatRoom.id = :roomId AND m.id < :cursor " +
            "ORDER BY m.id DESC")
    List<ChatMessage> findByRoomIdBeforeCursor(@Param("roomId") Long roomId,
                                               @Param("cursor") Long cursor,
                                               Pageable pageable);

    @Query("SELECT m FROM ChatMessage m " +
            "LEFT JOIN FETCH m.sender " +
            "WHERE m.chatRoom.id = :roomId " +
            "ORDER BY m.id DESC")
    List<ChatMessage> findLatestByRoomId(@Param("roomId") Long roomId, Pageable pageable);

    @Query("SELECT COUNT(m) FROM ChatMessage m " +
            "WHERE m.chatRoom.id = :roomId AND m.id > :lastReadMessageId")
    long countUnreadMessages(@Param("roomId") Long roomId,
                             @Param("lastReadMessageId") Long lastReadMessageId);

    @Query("SELECT m FROM ChatMessage m " +
            "LEFT JOIN FETCH m.sender " +
            "WHERE m.chatRoom.id = :roomId " +
            "ORDER BY m.id DESC")
    List<ChatMessage> findTopByRoomId(@Param("roomId") Long roomId, Pageable pageable);

    @Query("SELECT m FROM ChatMessage m " +
            "LEFT JOIN FETCH m.sender " +
            "WHERE m.chatRoom.id = :roomId AND m.id BETWEEN :startId AND :endId " +
            "ORDER BY m.id ASC")
    List<ChatMessage> findMessagesInRange(@Param("roomId") Long roomId,
                                          @Param("startId") Long startId,
                                          @Param("endId") Long endId);
}
