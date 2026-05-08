package com.matchhub.nyangvil.domain.boardcategory.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.matchhub.nyangvil.common.model.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "board_category_items")
@Getter
@Setter
@NoArgsConstructor
public class BoardCategoryItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String categoryCode;

    @Column(nullable = false, length = 50)
    private String label;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private BoardCategoryGroup group;

    @Column(nullable = false)
    private int displayOrder;

    @Column(nullable = false)
    private boolean active = true;

    public BoardCategoryItem(String categoryCode, String label, BoardCategoryGroup group, int displayOrder) {
        this.categoryCode = categoryCode;
        this.label = label;
        this.group = group;
        this.displayOrder = displayOrder;
    }
}
