package com.matchhub.catconnect.domain.auth.model.entity;

import com.matchhub.catconnect.common.model.entity.BaseEntity;
import com.matchhub.catconnect.domain.user.model.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Refresh Token 엔티티
 *
 * User와 ManyToOne 관계로 한 사용자가 여러 기기에서 로그인 가능
 * 토큰 원문을 저장하며, 추후 Redis 전환 시 해시 저장 방식으로 변경 가능
 */
@Entity
@Table(name = "tb_refresh_token", indexes = {
    @Index(name = "idx_refresh_token_token", columnList = "token"),
    @Index(name = "idx_refresh_token_user_id", columnList = "user_id")
})
@Getter
@NoArgsConstructor
public class RefreshToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 토큰 원문 저장 (Redis 전환 시 해시 저장으로 변경 고려)
    @Column(nullable = false, length = 500)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiredAt;

    public RefreshToken(User user, String token, LocalDateTime expiredAt) {
        this.user = user;
        this.token = token;
        this.expiredAt = expiredAt;
    }

    /**
     * Refresh Token Rotation 시 토큰과 만료일 갱신
     */
    public void updateToken(String newToken, LocalDateTime newExpiredAt) {
        this.token = newToken;
        this.expiredAt = newExpiredAt;
    }

    /**
     * 토큰 만료 여부 확인
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiredAt);
    }
}
