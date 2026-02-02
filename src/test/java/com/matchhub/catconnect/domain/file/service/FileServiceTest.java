package com.matchhub.catconnect.domain.file.service;

import com.matchhub.catconnect.domain.file.model.dto.FileResponseDTO;
import com.matchhub.catconnect.domain.file.model.enums.FileType;
import com.matchhub.catconnect.domain.file.repository.FileRepository;
import com.matchhub.catconnect.global.exception.AppException;
import com.matchhub.catconnect.global.exception.ErrorCode;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FileService 테스트")
@SpringBootTest
class FileServiceTest {

    private static final Logger log = LoggerFactory.getLogger(FileServiceTest.class);

    @Autowired
    private FileService fileService;

    @Autowired
    private FileRepository fileRepository;

    private FileResponseDTO testFile;

    @BeforeEach
    void setUp() {
        log.debug("테스트 설정 시작");

        // 테스트 전 DB 정리
        fileRepository.deleteAll();

        // 테스트용 이미지 파일 생성 및 업로드
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        testFile = fileService.uploadImage(mockFile, FileType.BOARD, 1L, "testUser");

        // 인증 정보 설정 (현재 사용자: testUser)
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("testUser", null, Collections.emptyList())
        );

        log.debug("테스트 설정 완료: fileId={}", testFile.getId());
    }

    @AfterEach
    void tearDown() {
        log.debug("테스트 정리 시작");
        try {
            fileRepository.deleteAll();
            SecurityContextHolder.clearContext();
            log.debug("테스트 정리 완료");
        } catch (Exception e) {
            log.debug("테스트 정리 실패: {}", e.getMessage());
        }
    }

    @Nested
    @DisplayName("파일 업로드 테스트")
    class FileUploadTests {

        @Test
        @DisplayName("이미지 파일 업로드 성공")
        void testUploadImageSuccess() {
            log.debug("이미지 파일 업로드 테스트 시작");

            // MockMultipartFile 생성
            MockMultipartFile mockFile = new MockMultipartFile(
                    "file",
                    "new-image.png",
                    "image/png",
                    "new image content".getBytes()
            );

            // 파일 업로드
            FileResponseDTO uploadedFile = fileService.uploadImage(mockFile, FileType.PROFILE, 2L, "testUser");

            // 업로드 결과 검증
            assertNotNull(uploadedFile.getId());
            assertEquals("new-image.png", uploadedFile.getOriginalName());
            assertEquals("image/png", uploadedFile.getContentType());
            assertEquals(FileType.PROFILE, uploadedFile.getFileType());
            assertEquals(2L, uploadedFile.getReferenceId());
            assertEquals("testUser", uploadedFile.getUploadedBy());
            assertNotNull(uploadedFile.getStoredName());
            assertTrue(uploadedFile.getDownloadUrl().contains(uploadedFile.getStoredName()));

            log.debug("이미지 파일 업로드 테스트 완료: fileId={}", uploadedFile.getId());
        }

        @Test
        @DisplayName("빈 파일 업로드 실패")
        void testUploadEmptyFile() {
            log.debug("빈 파일 업로드 테스트 시작");

            // 빈 파일 생성
            MockMultipartFile emptyFile = new MockMultipartFile(
                    "file",
                    "empty.jpg",
                    "image/jpeg",
                    new byte[0]
            );

            // 예외 발생 확인
            AppException exception = assertThrows(AppException.class, () ->
                    fileService.uploadImage(emptyFile, FileType.OTHER, null, "testUser")
            );
            assertEquals(ErrorCode.INVALID_REQUEST, exception.getErrorCode());

            log.debug("빈 파일 업로드 테스트 완료");
        }

        @Test
        @DisplayName("허용되지 않은 확장자 업로드 실패")
        void testUploadInvalidExtension() {
            log.debug("허용되지 않은 확장자 업로드 테스트 시작");

            // 허용되지 않은 확장자 파일 생성
            MockMultipartFile invalidFile = new MockMultipartFile(
                    "file",
                    "document.pdf",
                    "application/pdf",
                    "pdf content".getBytes()
            );

            // 예외 발생 확인
            AppException exception = assertThrows(AppException.class, () ->
                    fileService.uploadImage(invalidFile, FileType.OTHER, null, "testUser")
            );
            assertEquals(ErrorCode.INVALID_FILE_TYPE, exception.getErrorCode());

            log.debug("허용되지 않은 확장자 업로드 테스트 완료");
        }

        @Test
        @DisplayName("허용되지 않은 MIME 타입 업로드 실패")
        void testUploadInvalidMimeType() {
            log.debug("허용되지 않은 MIME 타입 업로드 테스트 시작");

            // 확장자는 jpg지만 MIME 타입이 text/plain인 파일
            MockMultipartFile invalidMimeFile = new MockMultipartFile(
                    "file",
                    "fake-image.jpg",
                    "text/plain",
                    "fake content".getBytes()
            );

            // 예외 발생 확인
            AppException exception = assertThrows(AppException.class, () ->
                    fileService.uploadImage(invalidMimeFile, FileType.OTHER, null, "testUser")
            );
            assertEquals(ErrorCode.INVALID_FILE_TYPE, exception.getErrorCode());

            log.debug("허용되지 않은 MIME 타입 업로드 테스트 완료");
        }

        @Test
        @DisplayName("다양한 이미지 형식 업로드 성공")
        void testUploadVariousImageFormats() {
            log.debug("다양한 이미지 형식 업로드 테스트 시작");

            // GIF 파일
            MockMultipartFile gifFile = new MockMultipartFile(
                    "file", "animation.gif", "image/gif", "gif content".getBytes()
            );
            FileResponseDTO gifResult = fileService.uploadImage(gifFile, FileType.OTHER, null, "testUser");
            assertEquals("image/gif", gifResult.getContentType());

            // WebP 파일
            MockMultipartFile webpFile = new MockMultipartFile(
                    "file", "modern.webp", "image/webp", "webp content".getBytes()
            );
            FileResponseDTO webpResult = fileService.uploadImage(webpFile, FileType.OTHER, null, "testUser");
            assertEquals("image/webp", webpResult.getContentType());

            log.debug("다양한 이미지 형식 업로드 테스트 완료");
        }
    }

    @Nested
    @DisplayName("파일 다운로드 테스트")
    class FileDownloadTests {

        @Test
        @DisplayName("파일 다운로드 성공")
        void testDownloadFileSuccess() {
            log.debug("파일 다운로드 테스트 시작");

            // 파일 다운로드
            Resource resource = fileService.downloadFile(testFile.getStoredName());

            // 다운로드 결과 검증
            assertNotNull(resource);
            assertTrue(resource.exists());

            log.debug("파일 다운로드 테스트 완료");
        }

        @Test
        @DisplayName("존재하지 않는 파일 다운로드 실패")
        void testDownloadFileNotFound() {
            log.debug("존재하지 않는 파일 다운로드 테스트 시작");

            // 존재하지 않는 파일명으로 다운로드 시도
            AppException exception = assertThrows(AppException.class, () ->
                    fileService.downloadFile("non-existent-file.jpg")
            );
            assertEquals(ErrorCode.FILE_NOT_FOUND, exception.getErrorCode());

            log.debug("존재하지 않는 파일 다운로드 테스트 완료");
        }
    }

    @Nested
    @DisplayName("파일 정보 조회 테스트")
    class FileInfoTests {

        @Test
        @DisplayName("파일 ID로 정보 조회 성공")
        void testGetFileInfoById() {
            log.debug("파일 ID로 정보 조회 테스트 시작");

            // 파일 정보 조회
            FileResponseDTO fileInfo = fileService.getFileInfo(testFile.getId());

            // 조회 결과 검증
            assertEquals(testFile.getId(), fileInfo.getId());
            assertEquals(testFile.getOriginalName(), fileInfo.getOriginalName());
            assertEquals(testFile.getStoredName(), fileInfo.getStoredName());

            log.debug("파일 ID로 정보 조회 테스트 완료");
        }

        @Test
        @DisplayName("존재하지 않는 파일 ID 조회 실패")
        void testGetFileInfoByIdNotFound() {
            log.debug("존재하지 않는 파일 ID 조회 테스트 시작");

            // 존재하지 않는 ID로 조회
            AppException exception = assertThrows(AppException.class, () ->
                    fileService.getFileInfo(999L)
            );
            assertEquals(ErrorCode.FILE_NOT_FOUND, exception.getErrorCode());

            log.debug("존재하지 않는 파일 ID 조회 테스트 완료");
        }

        @Test
        @DisplayName("저장된 파일명으로 정보 조회 성공")
        void testGetFileInfoByStoredName() {
            log.debug("저장된 파일명으로 정보 조회 테스트 시작");

            // 저장된 파일명으로 조회
            FileResponseDTO fileInfo = fileService.getFileInfoByStoredName(testFile.getStoredName());

            // 조회 결과 검증
            assertEquals(testFile.getId(), fileInfo.getId());
            assertEquals(testFile.getOriginalName(), fileInfo.getOriginalName());

            log.debug("저장된 파일명으로 정보 조회 테스트 완료");
        }

        @Test
        @DisplayName("참조별 파일 목록 조회 성공")
        void testGetFilesByReference() {
            log.debug("참조별 파일 목록 조회 테스트 시작");

            // 같은 참조에 추가 파일 업로드
            MockMultipartFile mockFile2 = new MockMultipartFile(
                    "file", "test2.jpg", "image/jpeg", "content2".getBytes()
            );
            fileService.uploadImage(mockFile2, FileType.BOARD, 1L, "testUser");

            // 참조별 파일 목록 조회
            List<FileResponseDTO> files = fileService.getFilesByReference(FileType.BOARD, 1L);

            // 조회 결과 검증
            assertEquals(2, files.size());
            assertTrue(files.stream().allMatch(f -> f.getFileType() == FileType.BOARD && f.getReferenceId() == 1L));

            log.debug("참조별 파일 목록 조회 테스트 완료");
        }

        @Test
        @DisplayName("전체 파일 페이지네이션 조회 성공")
        void testGetAllFilesWithPagination() {
            log.debug("전체 파일 페이지네이션 조회 테스트 시작");

            // 추가 파일 업로드
            for (int i = 0; i < 5; i++) {
                MockMultipartFile mockFile = new MockMultipartFile(
                        "file", "test" + i + ".jpg", "image/jpeg", ("content" + i).getBytes()
                );
                fileService.uploadImage(mockFile, FileType.OTHER, null, "testUser");
            }

            // 페이지네이션 조회
            Page<FileResponseDTO> filePage = fileService.getAllFiles(0, 3);

            // 조회 결과 검증
            assertEquals(3, filePage.getSize());
            assertEquals(6, filePage.getTotalElements()); // 기존 1개 + 추가 5개
            assertEquals(2, filePage.getTotalPages());

            log.debug("전체 파일 페이지네이션 조회 테스트 완료");
        }
    }

    @Nested
    @DisplayName("파일 삭제 테스트")
    class FileDeleteTests {

        @Test
        @DisplayName("본인 파일 삭제 성공")
        void testDeleteFileByOwner() {
            log.debug("본인 파일 삭제 테스트 시작");

            Long fileId = testFile.getId();

            // 파일 삭제
            fileService.deleteFile(fileId, "testUser");

            // 삭제 확인
            assertFalse(fileRepository.existsById(fileId));

            log.debug("본인 파일 삭제 테스트 완료");
        }

        @Test
        @DisplayName("타인 파일 삭제 실패")
        void testDeleteFileByOtherUser() {
            log.debug("타인 파일 삭제 테스트 시작");

            // 다른 사용자로 삭제 시도
            AppException exception = assertThrows(AppException.class, () ->
                    fileService.deleteFile(testFile.getId(), "otherUser")
            );
            assertEquals(ErrorCode.UNAUTHORIZED, exception.getErrorCode());

            // 파일이 삭제되지 않았는지 확인
            assertTrue(fileRepository.existsById(testFile.getId()));

            log.debug("타인 파일 삭제 테스트 완료");
        }

        @Test
        @DisplayName("관리자 파일 삭제 성공")
        void testDeleteFileByAdmin() {
            log.debug("관리자 파일 삭제 테스트 시작");

            Long fileId = testFile.getId();

            // 관리자 권한으로 삭제
            fileService.deleteFileByAdmin(fileId);

            // 삭제 확인
            assertFalse(fileRepository.existsById(fileId));

            log.debug("관리자 파일 삭제 테스트 완료");
        }

        @Test
        @DisplayName("존재하지 않는 파일 삭제 실패")
        void testDeleteFileNotFound() {
            log.debug("존재하지 않는 파일 삭제 테스트 시작");

            // 존재하지 않는 파일 삭제 시도
            AppException exception = assertThrows(AppException.class, () ->
                    fileService.deleteFile(999L, "testUser")
            );
            assertEquals(ErrorCode.FILE_NOT_FOUND, exception.getErrorCode());

            log.debug("존재하지 않는 파일 삭제 테스트 완료");
        }
    }
}
