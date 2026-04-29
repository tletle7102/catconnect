package com.matchhub.catconnect.domain.chat.repository;

import com.matchhub.catconnect.domain.chat.model.entity.ChatRoom;
import com.matchhub.catconnect.domain.chat.model.enums.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    @Query("SELECT cr FROM ChatRoom cr " +
            "JOIN cr.participants p1 " +
            "JOIN cr.participants p2 " +
            "WHERE cr.roomType = :roomType " +
            "AND p1.user.id = :userId1 AND p1.leftAt IS NULL " +
            "AND p2.user.id = :userId2 AND p2.leftAt IS NULL")
    Optional<ChatRoom> findDirectRoom(@Param("roomType") RoomType roomType,
                                      @Param("userId1") Long userId1,
                                      @Param("userId2") Long userId2);

    @Query("SELECT cr FROM ChatRoom cr " +
            "JOIN cr.participants p " +
            "WHERE cr.roomType = 'SUPPORT' " +
            "AND p.user.id = :userId AND p.leftAt IS NULL")
    Optional<ChatRoom> findSupportRoomByUserId(@Param("userId") Long userId);
}
