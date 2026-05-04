package com.matchhub.catconnect.global.configuration;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * 히어로 이미지 관리.
 * - 서버 시작 시 폴더에서 랜덤 1개 선택
 * - 관리자가 업로드하면 즉시 반영 (서버 재시작 불필요)
 */
@Component
public class HeroImageProvider {

    private static final Logger log = LoggerFactory.getLogger(HeroImageProvider.class);
    private static final Set<String> VALID_EXTENSIONS = Set.of("png", "jpg", "jpeg", "webp", "avif");
    private static final Set<String> IGNORED_FILES = Set.of("thumbs.db", ".ds_store");

    @Value("${app.hero.image-dir:${file.upload-dir}/hero-images}")
    private String imageDir;

    private volatile String selectedFileName;
    private volatile long version;

    @PostConstruct
    public void init() {
        version = System.currentTimeMillis() / 1000;
        Path dir = Paths.get(imageDir);

        if (!Files.exists(dir)) {
            try {
                Files.createDirectories(dir);
                log.info("히어로 이미지 디렉토리 생성: {}", dir);
            } catch (IOException e) {
                log.warn("히어로 이미지 디렉토리 생성 실패: {}", dir);
                return;
            }
        }

        selectRandomImage(dir);
    }

    /**
     * 관리자가 업로드한 이미지를 저장하고 즉시 히어로로 적용
     */
    public String uploadAndApply(MultipartFile file) throws IOException {
        String originalName = file.getOriginalFilename();
        if (originalName == null || !hasValidExtension(originalName)) {
            throw new IllegalArgumentException("허용되지 않는 파일 형식입니다.");
        }

        // UUID 파일명으로 저장
        String ext = originalName.substring(originalName.lastIndexOf('.'));
        String storedName = "hero_" + UUID.randomUUID().toString().substring(0, 8) + ext;

        Path dir = Paths.get(imageDir);
        if (!Files.exists(dir)) Files.createDirectories(dir);

        Path target = dir.resolve(storedName);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        // 즉시 반영
        selectedFileName = storedName;
        version = System.currentTimeMillis() / 1000;

        log.info("히어로 이미지 업로드 및 적용: {}", storedName);
        return storedName;
    }

    /**
     * 현재 선택된 이미지 목록 반환 (관리자 UI용)
     */
    public List<String> listImages() {
        Path dir = Paths.get(imageDir);
        if (!Files.exists(dir)) return List.of();

        try (Stream<Path> stream = Files.list(dir)) {
            return stream
                    .filter(Files::isRegularFile)
                    .map(p -> p.getFileName().toString())
                    .filter(name -> !name.startsWith("."))
                    .filter(name -> !IGNORED_FILES.contains(name.toLowerCase()))
                    .filter(this::hasValidExtension)
                    .toList();
        } catch (IOException e) {
            return List.of();
        }
    }

    /**
     * 기존 이미지 삭제
     */
    public void deleteImage(String fileName) throws IOException {
        Path file = Paths.get(imageDir).resolve(fileName);
        if (Files.exists(file)) {
            Files.delete(file);
            log.info("히어로 이미지 삭제: {}", fileName);
            // 삭제된 이미지가 현재 선택된 이미지면 재선택
            if (fileName.equals(selectedFileName)) {
                selectRandomImage(Paths.get(imageDir));
            }
        }
    }

    /**
     * 특정 이미지를 히어로로 선택
     */
    public void selectImage(String fileName) {
        selectedFileName = fileName;
        version = System.currentTimeMillis() / 1000;
        log.info("히어로 이미지 수동 선택: {}", fileName);
    }

    public String getImageUrl() {
        if (selectedFileName == null) return null;
        return "/uploads/hero-images/" + selectedFileName + "?v=" + version;
    }

    public boolean isFallback() {
        return selectedFileName == null;
    }

    public String getSelectedFileName() {
        return selectedFileName;
    }

    private void selectRandomImage(Path dir) {
        try (Stream<Path> stream = Files.list(dir)) {
            List<String> validFiles = stream
                    .filter(Files::isRegularFile)
                    .map(p -> p.getFileName().toString())
                    .filter(name -> !name.startsWith("."))
                    .filter(name -> !IGNORED_FILES.contains(name.toLowerCase()))
                    .filter(name -> !name.contains("_mobile.") && !name.contains("@2x."))
                    .filter(this::hasValidExtension)
                    .toList();

            if (validFiles.isEmpty()) {
                log.warn("히어로 이미지 폴더에 유효한 이미지가 없습니다: {}", dir);
                selectedFileName = null;
            } else {
                selectedFileName = validFiles.get(new Random().nextInt(validFiles.size()));
                log.info("히어로 이미지 선택됨: {} (총 {}개 중)", selectedFileName, validFiles.size());
            }
        } catch (IOException e) {
            log.warn("히어로 이미지 폴더 스캔 실패: {}", dir, e);
            selectedFileName = null;
        }
    }

    private boolean hasValidExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0) return false;
        String ext = fileName.substring(dotIndex + 1).toLowerCase();
        return VALID_EXTENSIONS.contains(ext);
    }
}
