package com.matchhub.catconnect.domain.settings.service;

import com.matchhub.catconnect.domain.settings.model.entity.SiteSetting;
import com.matchhub.catconnect.domain.settings.repository.SiteSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SiteSettingService {

    private final SiteSettingRepository repository;

    public String getValue(String key, String defaultValue) {
        return repository.findById(key)
                .map(SiteSetting::getValue)
                .orElse(defaultValue);
    }

    @Transactional
    public void setValue(String key, String value) {
        SiteSetting setting = repository.findById(key)
                .orElseGet(() -> new SiteSetting(key, value));
        setting.updateValue(value);
        repository.save(setting);
    }
}
