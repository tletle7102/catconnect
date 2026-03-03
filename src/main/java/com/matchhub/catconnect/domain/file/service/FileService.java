package com.matchhub.catconnect.domain.file.service;

import com.matchhub.catconnect.domain.file.model.dto.FileResponseDTO;
import com.matchhub.catconnect.domain.file.model.entity.FileEntity;
import com.matchhub.catconnect.domain.file.model.enums.FileType;
import com.matchhub.catconnect.domain.file.repository.FileRepository;
import com.matchhub.catconnect.global.exception.AppException;
import com.matchhub.catconnect.global.exception.Domain;
import com.matchhub.catconnect.global.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 파일 업로드/다운로드 서비스
 */
@Service
@Transactional(readOnly = true)
public class FileService {

    private static final Logger log = LoggerFactory.getLogger(FileService.class);

    // 허용된 이미지 확장자
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");

    // 허용된 MIME 타입
    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    private final FileRepository fileRepository;
    private final Path uploadPath;

    public FileService(FileRepository fileRepository,
                       @Value("${file.upload-dir:${user.home}/catconnect-uploads}") String uploadDir) {
        this.fileRepository = fileRepository;
        this.uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        createUploadDirectory();
    }

    private void createUploadDirectory() {
        try {
            Files.createDirectories(uploadPath);
            log.debug("업로드 디렉토리 생성/확인: {}", uploadPath);
        } catch (IOException e) {
            log.error("업로드 디렉토리 생성 실패: {}", uploadPath, e);
            throw new RuntimeException("업로드 디렉토리를 생성할 수 없습니다.", e);
        }
    }

    /**
     * 이미지 파일 업로드
     */
    @Transactional
    public FileResponseDTO uploadImage(MultipartFile file, FileType fileType, Long referenceId, String username) {
        log.debug("이미지 업로드 시작: originalName={}, fileType={}, referenceId={}, username={}",
                file.getOriginalFilename(), fileType, referenceId, username);

        // 파일 유효성 검사
        validateFile(file);

        // 저장할 파일명 생성 (UUID + 확장자)
        String originalName = file.getOriginalFilename();
        String extension = getExtension(originalName);
        String storedName = UUID.randomUUID().toString() + "." + extension;

        // 파일 저장
        Path targetPath = uploadPath.resolve(storedName);
        try {
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            log.debug("파일 저장 완료: {}", targetPath);
        } catch (IOException e) {
            log.error("파일 저장 실패: {}", targetPath, e);
            throw new AppException(Domain.NONE, ErrorCode.FILE_UPLOAD_FAILED,
                    "파일 저장 중 오류가 발생했습니다: " + e.getMessage());
        }

        // 메타데이터 저장
        FileEntity fileEntity = new FileEntity(
                originalName,
                storedName,
                targetPath.toString(),
                file.getContentType(),
                file.getSize(),
                fileType,
                referenceId,
                username
        );
        fileRepository.save(fileEntity);
        log.debug("파일 메타데이터 저장 완료: id={}", fileEntity.getId());

        return new FileResponseDTO(fileEntity);
    }

    /**
     * 파일 다운로드
     */
    public Resource downloadFile(String storedName) {
        log.debug("파일 다운로드 요청: storedName={}", storedName);

        FileEntity fileEntity = fileRepository.findByStoredName(storedName)
                .orElseThrow(() -> new AppException(Domain.NONE, ErrorCode.FILE_NOT_FOUND,
                        "파일을 찾을 수 없습니다: " + storedName));

        try {
            Path filePath = Paths.get(fileEntity.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                log.debug("파일 다운로드 성공: {}", filePath);
                return resource;
            } else {
                log.error("파일을 읽을 수 없습니다: {}", filePath);
                throw new AppException(Domain.NONE, ErrorCode.FILE_NOT_FOUND,
                        "파일을 읽을 수 없습니다: " + storedName);
            }
        } catch (MalformedURLException e) {
            log.error("잘못된 파일 경로: {}", fileEntity.getFilePath(), e);
            throw new AppException(Domain.NONE, ErrorCode.FILE_NOT_FOUND,
                    "잘못된 파일 경로입니다: " + storedName);
        }
    }

    /**
     * 파일 정보 조회 (ID로)
     */
    public FileResponseDTO getFileInfo(Long fileId) {
        log.debug("파일 정보 조회: fileId={}", fileId);
        FileEntity fileEntity = fileRepository.findById(fileId)
                .orElseThrow(() -> new AppException(Domain.NONE, ErrorCode.FILE_NOT_FOUND,
                        "파일을 찾을 수 없습니다: id=" + fileId));
        return new FileResponseDTO(fileEntity);
    }

    /**
     * 파일 정보 조회 (저장된 파일명으로)
     */
    public FileResponseDTO getFileInfoByStoredName(String storedName) {
        log.debug("파일 정보 조회: storedName={}", storedName);
        FileEntity fileEntity = fileRepository.findByStoredName(storedName)
                .orElseThrow(() -> new AppException(Domain.NONE, ErrorCode.FILE_NOT_FOUND,
                        "파일을 찾을 수 없습니다: " + storedName));
        return new FileResponseDTO(fileEntity);
    }

    /**
     * 참조별 파일 목록 조회
     */
    public List<FileResponseDTO> getFilesByReference(FileType fileType, Long referenceId) {
        log.debug("참조별 파일 목록 조회: fileType={}, referenceId={}", fileType, referenceId);
        return fileRepository.findByFileTypeAndReferenceId(fileType, referenceId)
                .stream()
                .map(FileResponseDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * 전체 파일 목록 조회 (페이지네이션)
     */
    public Page<FileResponseDTO> getAllFiles(int page, int size) {
        log.debug("전체 파일 목록 조회: page={}, size={}", page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdDttm"));
        return fileRepository.findAll(pageable).map(FileResponseDTO::new);
    }

    /**
     * 파일 삭제 (본인 파일만)
     */
    @Transactional
    public void deleteFile(Long fileId, String username) {
        log.debug("파일 삭제 요청: fileId={}, username={}", fileId, username);

        FileEntity fileEntity = fileRepository.findById(fileId)
                .orElseThrow(() -> new AppException(Domain.NONE, ErrorCode.FILE_NOT_FOUND,
                        "파일을 찾을 수 없습니다: id=" + fileId));

        // 권한 확인 (본인 파일만 삭제 가능)
        if (!fileEntity.getUploadedBy().equals(username)) {
            log.warn("파일 삭제 권한 없음: fileId={}, owner={}, requester={}",
                    fileId, fileEntity.getUploadedBy(), username);
            throw new AppException(Domain.NONE, ErrorCode.UNAUTHORIZED,
                    "본인이 업로드한 파일만 삭제할 수 있습니다.");
        }

        deleteFileInternal(fileEntity);
    }

    /**
     * 파일 삭제 (관리자용)
     */
    @Transactional
    public void deleteFileByAdmin(Long fileId) {
        log.debug("관리자 파일 삭제 요청: fileId={}", fileId);

        FileEntity fileEntity = fileRepository.findById(fileId)
                .orElseThrow(() -> new AppException(Domain.NONE, ErrorCode.FILE_NOT_FOUND,
                        "파일을 찾을 수 없습니다: id=" + fileId));

        deleteFileInternal(fileEntity);
    }

    private void deleteFileInternal(FileEntity fileEntity) {
        // 물리적 파일 삭제
        try {
            Path filePath = Paths.get(fileEntity.getFilePath());
            Files.deleteIfExists(filePath);
            log.debug("물리적 파일 삭제 완료: {}", filePath);
        } catch (IOException e) {
            log.error("물리적 파일 삭제 실패: {}", fileEntity.getFilePath(), e);
        }

        // DB에서 메타데이터 삭제
        fileRepository.delete(fileEntity);
        log.debug("파일 메타데이터 삭제 완료: id={}", fileEntity.getId());
    }

    /**
     * 파일 유효성 검사
     */
    private void validateFile(MultipartFile file) {
        // 빈 파일 체크
        if (file.isEmpty()) {
            throw new AppException(Domain.NONE, ErrorCode.INVALID_REQUEST, "파일이 비어있습니다.");
        }

        String originalName = file.getOriginalFilename();
        String extension = getExtension(originalName).toLowerCase();
        String contentType = file.getContentType();

        // 확장자 검사
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            log.warn("허용되지 않은 파일 확장자: {}", extension);
            throw new AppException(Domain.NONE, ErrorCode.INVALID_FILE_TYPE,
                    "허용되지 않은 파일 형식입니다. 허용된 형식: " + String.join(", ", ALLOWED_EXTENSIONS));
        }

        // MIME 타입 검사
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType)) {
            log.warn("허용되지 않은 MIME 타입: {}", contentType);
            throw new AppException(Domain.NONE, ErrorCode.INVALID_FILE_TYPE,
                    "허용되지 않은 파일 형식입니다. 이미지 파일만 업로드 가능합니다.");
        }

        log.debug("파일 유효성 검사 통과: name={}, extension={}, contentType={}, size={}",
                originalName, extension, contentType, file.getSize());
    }

    /**
     * 파일 확장자 추출
     */
    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}
