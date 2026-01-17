package com.matchhub.catconnect.domain.file.model.dto;

import com.matchhub.catconnect.domain.file.model.entity.FileEntity;
import com.matchhub.catconnect.domain.file.model.enums.FileType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.apache.catalina.webresources.FileResource;

import java.time.LocalDateTime;

/**
 * 파일 응답 DTO
 */
@Schema(description = "파일 응답 정보")
@Getter
public class FileResponseDTO {

    @Schema(description = "파일 ID", example = "1")
    private Long id;

    @Schema(description = "원본 파일명", example = "profile.jpg")
    private String originalName;

    @Schema(description = "저장된 파일명", example = "550e8400-e29b-41d4-a716-446655440000.jpg")
    private String storedName;

    @Schema(description = "MIME 타입", example = "image/jpeg")
    private String contentType;

    @Schema(description = "파일 크기 (bytes)", example = "102400")
    private Long fileSize;

    @Schema(description = "파일 타입", example = "PROFILE")
    private FileType fileType;

    @Schema(description = "참조 ID", example = "1")
    private Long referenceId;

    @Schema(description = "업로드한 사용자", example = "testuser")
    private String uploadedBy;

    @Schema(description = "다운로드 URL", example = "/api/files/download/550e8400-e29b-41d4-a716-446655440000.jpg")
    private String downloadUrl;

    @Schema(description = "생성일시")
    private LocalDateTime createdDttm;

    public FileResponseDTO(FileEntity entity) {
        this.id = entity.getId();
        this.originalName = entity.getOriginalName();
        this.storedName = entity.getStoredName();
        this.contentType = entity.getContentType();
        this.fileSize = entity.getFileSize();
        this.fileType = entity.getFileType();
        this.referenceId = entity.getReferenceId();
        this.uploadedBy = entity.getUploadedBy();
        this.downloadUrl = "/api/files/download/" + entity.getStoredName();
        this.createdDttm = entity.getCreatedDttm();
    }
}
