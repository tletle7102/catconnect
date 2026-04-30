package com.matchhub.catconnect.domain.block.model.entity;

import com.matchhub.catconnect.common.model.entity.BaseEntity;
import com.matchhub.catconnect.domain.user.model.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_blocks",
        uniqueConstraints = @UniqueConstraint(columnNames = {"blocker_id", "blocked_id"}),
        indexes = {
                @Index(name = "idx_blocks_blocker", columnList = "blocker_id"),
                @Index(name = "idx_blocks_blocked", columnList = "blocked_id")
        })
@Getter
@NoArgsConstructor
public class UserBlock extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocker_id", nullable = false)
    private User blocker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocked_id", nullable = false)
    private User blocked;

    public UserBlock(User blocker, User blocked) {
        this.blocker = blocker;
        this.blocked = blocked;
    }
}
