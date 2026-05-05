package com.matchhub.catconnect.domain.boardcategory.model.entity;

import com.matchhub.catconnect.common.model.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "board_category_groups")
@Getter
@Setter
@NoArgsConstructor
public class BoardCategoryGroup extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String label;

    @Column(length = 10)
    private String icon;

    @Column(nullable = false)
    private int displayOrder;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<BoardCategoryItem> items = new ArrayList<>();

    public BoardCategoryGroup(String label, String icon, int displayOrder) {
        this.label = label;
        this.icon = icon;
        this.displayOrder = displayOrder;
    }
}
