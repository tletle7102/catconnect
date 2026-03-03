package com.matchhub.catconnect.domain.file.repository;

import com.matchhub.catconnect.domain.file.model.entity.FileEntity;
import com.matchhub.catconnect.domain.file.model.enums.FileType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 파일 레포지토리
 */
@Repository
public interface FileRepository extends JpaRepository<FileEntity, Long> {

    Optional<FileEntity> findByStoredName(String storedName);

    List<FileEntity> findByFileTypeAndReferenceId(FileType fileType, Long referenceId);

    List<FileEntity> findByUploadedBy(String uploadedBy);
}
