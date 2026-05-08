package com.matchhub.nyangvil.domain.board.model.entity;

import com.matchhub.nyangvil.common.model.entity.BaseEntity;
import com.matchhub.nyangvil.domain.board.model.enums.PrefixMode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "board_category_settings")
@Getter
@NoArgsConstructor
public class BoardCategorySetting extends BaseEntity {

    @Id
    @Column(length = 30)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private PrefixMode prefixMode = PrefixMode.NONE;

    @Column(length = 100)
    private String prefixFixedValue;

    @Column(columnDefinition = "TEXT")
    private String prefixListItems;

    public BoardCategorySetting(String category) {
        this.category = category;
        this.prefixMode = PrefixMode.NONE;
    }

    public void updatePrefixMode(PrefixMode mode) {
        this.prefixMode = mode;
    }

    public void updatePrefixFixedValue(String value) {
        this.prefixFixedValue = value;
    }

    public void updatePrefixListItems(String items) {
        this.prefixListItems = items;
    }

    public String[] getPrefixListArray() {
        if (prefixListItems == null || prefixListItems.isBlank()) return new String[0];
        return prefixListItems.split(",");
    }
}
