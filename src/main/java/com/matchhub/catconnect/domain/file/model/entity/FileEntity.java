package com.matchhub.catconnect.domain.file.model.entity;

import com.matchhub.catconnect.common.model.entity.BaseEntity;
import com.matchhub.catconnect.domain.file.model.enums.FileType;
import jakarta.persistence.*;
import jdk.jfr.Enabled;
import lombok.Generated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.cfg.defs.AssertFalseDef;

/**
 * 파일 메타데이터 엔티티
 */
@Entity
@Table(name = "files")
@Getter
@NoArgsConstructor
public class FileEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String originalName;  // 원본 파일명

    @Column(nullable = false, unique = true)
    private String storedName;    // 저장된 파일명 (UUID)

    @Column(nullable = false)
    private String filePath;      // 파일 저장 경로

    @Column(nullable = false)
    private String contentType;   // MIME 타입

    @Column(nullable = false)
    private Long fileSize;        // 파일 크기 (bytes)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FileType fileType;    // 파일 타입 (PROFILE, BOARD, OTHER)

    private Long referenceId;     // 참조 ID (게시글 ID, 사용자 ID 등)

    @Column(nullable = false)
    private String uploadedBy;    // 업로드한 사용자

    public FileEntity(String originalName, String storedName, String filePath,
                      String contentType, Long fileSize, FileType fileType,
                      Long referenceId, String uploadedBy) {
        this.originalName = originalName;
        this.storedName = storedName;
        this.filePath = filePath;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.fileType = fileType;
        this.referenceId = referenceId;
        this.uploadedBy = uploadedBy;
    }
}
