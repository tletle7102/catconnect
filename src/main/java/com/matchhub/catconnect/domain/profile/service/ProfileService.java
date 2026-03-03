package com.matchhub.catconnect.domain.profile.service;

import com.matchhub.catconnect.domain.file.model.dto.FileResponseDTO;
import com.matchhub.catconnect.domain.file.model.enums.FileType;
import com.matchhub.catconnect.domain.file.service.FileService;
import com.matchhub.catconnect.domain.user.model.entity.User;
import com.matchhub.catconnect.domain.user.repository.UserRepository;
import com.matchhub.catconnect.global.exception.AppException;
import com.matchhub.catconnect.global.exception.Domain;
import com.matchhub.catconnect.global.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * 프로필 관리 서비스
 */
@Service
@Transactional
public class ProfileService {

    private static final Logger log = LoggerFactory.getLogger(ProfileService.class);

    private final UserRepository userRepository;
    private final FileService fileService;

    public ProfileService(UserRepository userRepository, FileService fileService) {
        this.userRepository = userRepository;
        this.fileService = fileService;
    }

    /**
     * 프로필 이미지 업로드 및 사용자 정보 업데이트
     * @param username 사용자 이름
     * @param file 업로드할 이미지 파일
     * @return 업로드된 파일 정보
     */
    public FileResponseDTO uploadProfileImage(String username, MultipartFile file) {
        log.debug("프로필 이미지 업로드: username={}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(Domain.USER, ErrorCode.USER_NOT_FOUND,
                        "사용자를 찾을 수 없습니다."));

        // 파일 업로드
        FileResponseDTO fileResponse = fileService.uploadImage(file, FileType.PROFILE, user.getId(), username);

        // 사용자 프로필 이미지 URL 업데이트
        String profileImageUrl = "/api/files/download/" + fileResponse.getStoredName();
        user.updateProfileImage(profileImageUrl);
        userRepository.save(user);

        log.debug("프로필 이미지 업로드 완료: username={}, url={}", username, profileImageUrl);
        return fileResponse;
    }

    /**
     * 프로필 이미지 삭제
     * @param username 사용자 이름
     */
    public void deleteProfileImage(String username) {
        log.debug("프로필 이미지 삭제: username={}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(Domain.USER, ErrorCode.USER_NOT_FOUND,
                        "사용자를 찾을 수 없습니다."));

        // 프로필 이미지 URL 초기화
        user.updateProfileImage(null);
        userRepository.save(user);

        log.debug("프로필 이미지 삭제 완료: username={}", username);
    }

    /**
     * 사용자 프로필 이미지 URL 조회
     * @param username 사용자 이름
     * @return 프로필 이미지 URL (없으면 null)
     */
    @Transactional(readOnly = true)
    public String getProfileImageUrl(String username) {
        log.debug("프로필 이미지 URL 조회: username={}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(Domain.USER, ErrorCode.USER_NOT_FOUND,
                        "사용자를 찾을 수 없습니다."));

        return user.getProfileImageUrl();
    }
}
