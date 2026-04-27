package com.matchhub.catconnect.domain.report.model.entity;

import com.matchhub.catconnect.common.model.entity.BaseEntity;
import com.matchhub.catconnect.domain.report.model.enums.SanctionType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_user_sanction")
@Getter
@NoArgsConstructor
public class UserSanction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "사용자명은 필수입니다.")
    @Column(nullable = false)
    private String username;

    @NotNull(message = "제재 유형은 필수입니다.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SanctionType sanctionType;

    @Column
    private String reason;

    @Column
    private LocalDateTime expiresAt;

    public UserSanction(String username, SanctionType sanctionType, String reason, LocalDateTime expiresAt) {
        this.username = username;
        this.sanctionType = sanctionType;
        this.reason = reason;
        this.expiresAt = expiresAt;
    }

    /**
     * 제재가 현재 유효한지 확인
     * expiresAt이 null이면 영구 제재이므로 항상 true
     * expiresAt이 현재 시간 이후이면 true
     */
    public boolean isActive() {
        return expiresAt == null || expiresAt.isAfter(LocalDateTime.now());
    }
}
