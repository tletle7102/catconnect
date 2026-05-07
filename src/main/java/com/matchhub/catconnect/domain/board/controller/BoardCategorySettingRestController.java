package com.matchhub.catconnect.domain.board.controller;

import com.matchhub.catconnect.domain.board.model.entity.BoardCategorySetting;
import com.matchhub.catconnect.domain.board.model.enums.PrefixMode;
import com.matchhub.catconnect.domain.board.service.BoardCategorySettingService;
import com.matchhub.catconnect.global.exception.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/board-settings")
@RequiredArgsConstructor
public class BoardCategorySettingRestController {

    private final BoardCategorySettingService settingService;

    @GetMapping
    public ResponseEntity<Response<List<BoardCategorySetting>>> getAllSettings() {
        return ResponseEntity.ok(Response.success(settingService.getAllSettings()));
    }

    @GetMapping("/{category}")
    public ResponseEntity<Response<BoardCategorySetting>> getSetting(@PathVariable String category) {
        return ResponseEntity.ok(Response.success(settingService.getSetting(category)));
    }

    @PutMapping("/{category}")
    public ResponseEntity<Response<BoardCategorySetting>> updateSetting(
            @PathVariable String category,
            @RequestBody Map<String, String> request) {
        PrefixMode mode = PrefixMode.valueOf(request.getOrDefault("prefixMode", "NONE"));
        String fixedValue = request.get("prefixFixedValue");
        String listItems = request.get("prefixListItems");
        BoardCategorySetting setting = settingService.updateSetting(category, mode, fixedValue, listItems);
        return ResponseEntity.ok(Response.success(setting));
    }
}
