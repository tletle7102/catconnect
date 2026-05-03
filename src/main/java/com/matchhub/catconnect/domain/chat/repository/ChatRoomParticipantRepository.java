package com.matchhub.catconnect.domain.chat.repository;

import com.matchhub.catconnect.domain.chat.model.entity.ChatRoomParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomParticipantRepository extends JpaRepository<ChatRoomParticipant, Long> {

    @Query("SELECT p FROM ChatRoomParticipant p " +
            "JOIN FETCH p.chatRoom " +
            "JOIN FETCH p.user " +
            "WHERE p.user.id = :userId AND p.leftAt IS NULL " +
            "ORDER BY p.chatRoom.updatedDttm DESC")
    List<ChatRoomParticipant> findActiveByUserId(@Param("userId") Long userId);

    @Query("SELECT p FROM ChatRoomParticipant p " +
            "WHERE p.chatRoom.id = :roomId AND p.user.id = :userId")
    Optional<ChatRoomParticipant> findByRoomIdAndUserId(@Param("roomId") Long roomId,
                                                        @Param("userId") Long userId);

    @Query("SELECT p FROM ChatRoomParticipant p " +
            "JOIN FETCH p.user " +
            "WHERE p.chatRoom.id = :roomId AND p.user.id != :userId AND p.leftAt IS NULL")
    Optional<ChatRoomParticipant> findOtherParticipant(@Param("roomId") Long roomId,
                                                       @Param("userId") Long userId);
}
