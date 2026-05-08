package com.matchhub.nyangvil.domain.boardcategory.model.entity;

import com.matchhub.nyangvil.common.model.entity.BaseEntity;
import com.matchhub.nyangvil.domain.user.model.enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "board_permissions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"category_code", "role"}))
@Getter
@Setter
@NoArgsConstructor
public class BoardPermission extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category_code", nullable = false, length = 30)
    private String categoryCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Role role;

    @Column(nullable = false)
    private boolean canRead = true;

    @Column(nullable = false)
    private boolean canWrite = true;

    public BoardPermission(String categoryCode, Role role, boolean canRead, boolean canWrite) {
        this.categoryCode = categoryCode;
        this.role = role;
        this.canRead = canRead;
        this.canWrite = canWrite;
    }
}
