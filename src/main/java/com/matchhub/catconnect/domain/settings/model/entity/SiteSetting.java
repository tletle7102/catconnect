package com.matchhub.catconnect.domain.settings.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "site_settings")
@Getter
@NoArgsConstructor
public class SiteSetting {

    @Id
    @Column(name = "setting_key", length = 50)
    private String key;

    @Column(name = "setting_value", length = 500)
    private String value;

    @Column(name = "updated_dttm")
    private LocalDateTime updatedDttm;

    public SiteSetting(String key, String value) {
        this.key = key;
        this.value = value;
        this.updatedDttm = LocalDateTime.now();
    }

    public void updateValue(String value) {
        this.value = value;
        this.updatedDttm = LocalDateTime.now();
    }
}
