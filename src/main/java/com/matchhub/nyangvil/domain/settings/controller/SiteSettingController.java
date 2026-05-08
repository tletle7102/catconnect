package com.matchhub.nyangvil.domain.settings.controller;

import com.matchhub.nyangvil.domain.settings.service.SiteSettingService;
import com.matchhub.nyangvil.global.configuration.HeroImageProvider;
import com.matchhub.nyangvil.global.exception.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class SiteSettingController {

    private final SiteSettingService siteSettingService;
    private final HeroImageProvider heroImageProvider;

    // === 공개 API ===

    @GetMapping("/api/site-settings/popular-default-tab")
    public ResponseEntity<Response<Map<String, String>>> getPopularDefaultTab() {
        String value = siteSettingService.getValue("popular_default_tab", "LIKE");
        return ResponseEntity.ok(Response.success(Map.of("value", value)));
    }

    @GetMapping("/api/site-settings/hero-image")
    public ResponseEntity<Response<Map<String, Object>>> getHeroImage() {
        String imageUrl = heroImageProvider.getImageUrl();
        boolean fallback = heroImageProvider.isFallback();
        return ResponseEntity.ok(Response.success(Map.of(
                "imageUrl", imageUrl != null ? imageUrl : "",
                "fallback", fallback
        )));
    }

    // === 관리자 API ===

    @PutMapping("/api/admin/site-settings/popular-default-tab")
    public ResponseEntity<Response<Map<String, String>>> setPopularDefaultTab(@RequestBody Map<String, String> request) {
        String value = request.getOrDefault("value", "LIKE");
        siteSettingService.setValue("popular_default_tab", value);
        return ResponseEntity.ok(Response.success(Map.of("value", value)));
    }

    // 히어로 이미지 목록 조회
    @GetMapping("/api/admin/hero-images")
    public ResponseEntity<Response<Map<String, Object>>> listHeroImages() {
        List<String> images = heroImageProvider.listImages();
        String selected = heroImageProvider.getSelectedFileName();
        return ResponseEntity.ok(Response.success(Map.of(
                "images", images,
                "selected", selected != null ? selected : ""
        )));
    }

    // 히어로 이미지 업로드 + 즉시 적용
    @PostMapping("/api/admin/hero-images/upload")
    public ResponseEntity<Response<Map<String, String>>> uploadHeroImage(@RequestParam("file") MultipartFile file) throws IOException {
        String storedName = heroImageProvider.uploadAndApply(file);
        return ResponseEntity.ok(Response.success(Map.of(
                "fileName", storedName,
                "imageUrl", heroImageProvider.getImageUrl()
        ), "히어로 이미지가 변경되었습니다."));
    }

    // 기존 이미지 중 하나를 선택하여 적용
    @PutMapping("/api/admin/hero-images/select")
    public ResponseEntity<Response<Map<String, String>>> selectHeroImage(@RequestBody Map<String, String> request) {
        String fileName = request.get("fileName");
        heroImageProvider.selectImage(fileName);
        return ResponseEntity.ok(Response.success(Map.of(
                "fileName", fileName,
                "imageUrl", heroImageProvider.getImageUrl()
        ), "히어로 이미지가 변경되었습니다."));
    }

    // 이미지 삭제
    @DeleteMapping("/api/admin/hero-images/{fileName}")
    public ResponseEntity<Response<Void>> deleteHeroImage(@PathVariable String fileName) throws IOException {
        heroImageProvider.deleteImage(fileName);
        return ResponseEntity.ok(Response.success(null, "이미지가 삭제되었습니다."));
    }
}
