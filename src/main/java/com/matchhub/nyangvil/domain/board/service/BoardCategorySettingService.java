package com.matchhub.nyangvil.domain.board.service;

import com.matchhub.nyangvil.domain.board.model.entity.BoardCategorySetting;
import com.matchhub.nyangvil.domain.board.model.enums.PrefixMode;
import com.matchhub.nyangvil.domain.board.repository.BoardCategorySettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardCategorySettingService {

    private final BoardCategorySettingRepository repository;

    public List<BoardCategorySetting> getAllSettings() {
        return repository.findAll();
    }

    public BoardCategorySetting getSetting(String category) {
        return repository.findById(category)
                .orElseGet(() -> new BoardCategorySetting(category));
    }

    @Transactional
    public BoardCategorySetting updateSetting(String category, PrefixMode mode,
                                               String fixedValue, String listItems) {
        BoardCategorySetting setting = repository.findById(category)
                .orElseGet(() -> new BoardCategorySetting(category));

        setting.updatePrefixMode(mode);
        setting.updatePrefixFixedValue(fixedValue);
        setting.updatePrefixListItems(listItems);

        return repository.save(setting);
    }
}
