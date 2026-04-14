package com.matchhub.catconnect.domain.auth.repository;

import com.matchhub.catconnect.domain.auth.model.entity.RefreshToken;
import com.matchhub.catconnect.domain.user.model.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Refresh Token 레포지토리
 *
 * 토큰 조회, 사용자별 토큰 삭제, 만료된 토큰 정리 기능 제공
 */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * 토큰 문자열로 RefreshToken 조회 (User 즉시 로딩)
     */
    @EntityGraph(attributePaths = {"user"})
    Optional<RefreshToken> findByToken(String token);

    /**
     * 사용자의 모든 Refresh Token 삭제 (전체 로그아웃)
     */
    void deleteByUser(User user);

    /**
     * 사용자 ID로 모든 Refresh Token 삭제
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    /**
     * 만료된 토큰 일괄 삭제 (스케줄러에서 사용)
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiredAt < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * 특정 토큰 삭제
     */
    void deleteByToken(String token);

    /**
     * 사용자의 Refresh Token 존재 여부 확인
     */
    boolean existsByUser(User user);
}
