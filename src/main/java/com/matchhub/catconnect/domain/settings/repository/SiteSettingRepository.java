package com.matchhub.catconnect.domain.settings.repository;

import com.matchhub.catconnect.domain.settings.model.entity.SiteSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SiteSettingRepository extends JpaRepository<SiteSetting, String> {
}
