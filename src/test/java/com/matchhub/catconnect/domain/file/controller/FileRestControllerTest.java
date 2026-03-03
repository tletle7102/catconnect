package com.matchhub.catconnect.domain.file.controller;

import com.matchhub.catconnect.domain.file.model.dto.FileResponseDTO;
import com.matchhub.catconnect.domain.file.model.enums.FileType;
import com.matchhub.catconnect.domain.file.repository.FileRepository;
import com.matchhub.catconnect.domain.file.service.FileService;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("FileRestController 테스트")
@SpringBootTest
@AutoConfigureMockMvc
class FileRestControllerTest {

    private static final Logger log = LoggerFactory.getLogger(FileRestControllerTest.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FileService fileService;

    @Autowired
    private FileRepository fileRepository;

    private FileResponseDTO testFile;

    @BeforeEach
    void setUp() {
        log.debug("테스트 설정 시작");

        // 기존 파일 데이터 전체 삭제
        fileRepository.deleteAll();

        // 테스트용 파일 업로드
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );
        testFile = fileService.uploadImage(mockFile, FileType.BOARD, 1L, "testUser");

        log.debug("테스트 설정 완료: fileId={}", testFile.getId());
    }

    @AfterEach
    void tearDown() {
        log.debug("테스트 정리 시작");
        try {
            fileRepository.deleteAll();
        } catch (Exception e) {
            log.debug("테스트 정리 실패: {}", e.getMessage());
        }
        log.debug("테스트 정리 완료");
    }

    @Nested
    @DisplayName("파일 업로드 API 테스트")
    class FileUploadApiTests {

        @Test
        @WithMockUser(username = "testUser")
        @DisplayName("이미지 파일 업로드 성공")
        void testUploadImage() throws Exception {
            log.debug("이미지 파일 업로드 테스트 시작");

            MockMultipartFile uploadFile = new MockMultipartFile(
                    "file",
                    "new-image.png",
                    "image/png",
                    "new image content".getBytes()
            );

            mockMvc.perform(multipart("/api/files/upload")
                            .file(uploadFile)
                            .param("fileType", "PROFILE")
                            .param("referenceId", "2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.originalName").value("new-image.png"))
                    .andExpect(jsonPath("$.data.contentType").value("image/png"))
                    .andExpect(jsonPath("$.data.fileType").value("PROFILE"))
                    .andExpect(jsonPath("$.data.uploadedBy").value("testUser"))
                    .andDo(result -> log.debug("이미지 파일 업로드 응답: {}", result.getResponse().getContentAsString()));

            log.debug("이미지 파일 업로드 테스트 완료");
        }

        @Test
        @DisplayName("미인증 사용자 파일 업로드 실패")
        void testUploadImageUnauthorized() throws Exception {
            log.debug("미인증 사용자 파일 업로드 테스트 시작");

            MockMultipartFile uploadFile = new MockMultipartFile(
                    "file",
                    "test.jpg",
                    "image/jpeg",
                    "content".getBytes()
            );

            mockMvc.perform(multipart("/api/files/upload")
                            .file(uploadFile)
                            .param("fileType", "OTHER"))
                    .andExpect(status().isUnauthorized())
                    .andDo(result -> log.debug("미인증 사용자 파일 업로드 응답: {}", result.getResponse().getContentAsString()));

            log.debug("미인증 사용자 파일 업로드 테스트 완료");
        }

        @Test
        @WithMockUser(username = "testUser")
        @DisplayName("허용되지 않은 파일 형식 업로드 실패")
        void testUploadInvalidFileType() throws Exception {
            log.debug("허용되지 않은 파일 형식 업로드 테스트 시작");

            MockMultipartFile invalidFile = new MockMultipartFile(
                    "file",
                    "document.pdf",
                    "application/pdf",
                    "pdf content".getBytes()
            );

            mockMvc.perform(multipart("/api/files/upload")
                            .file(invalidFile)
                            .param("fileType", "OTHER"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("FILE_003"))
                    .andDo(result -> log.debug("허용되지 않은 파일 형식 업로드 응답: {}", result.getResponse().getContentAsString()));

            log.debug("허용되지 않은 파일 형식 업로드 테스트 완료");
        }

        @Test
        @WithMockUser(username = "testUser")
        @DisplayName("기본 파일 타입으로 업로드 성공")
        void testUploadWithDefaultFileType() throws Exception {
            log.debug("기본 파일 타입 업로드 테스트 시작");

            MockMultipartFile uploadFile = new MockMultipartFile(
                    "file",
                    "default-type.jpg",
                    "image/jpeg",
                    "content".getBytes()
            );

            mockMvc.perform(multipart("/api/files/upload")
                            .file(uploadFile))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.fileType").value("OTHER"))
                    .andDo(result -> log.debug("기본 파일 타입 업로드 응답: {}", result.getResponse().getContentAsString()));

            log.debug("기본 파일 타입 업로드 테스트 완료");
        }
    }

    @Nested
    @DisplayName("파일 다운로드 API 테스트")
    class FileDownloadApiTests {

        @Test
        @DisplayName("파일 다운로드 성공")
        void testDownloadFile() throws Exception {
            log.debug("파일 다운로드 테스트 시작");

            mockMvc.perform(get("/api/files/download/" + testFile.getStoredName()))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Disposition",
                            org.hamcrest.Matchers.containsString("filename")))
                    .andDo(result -> log.debug("파일 다운로드 응답 상태: {}", result.getResponse().getStatus()));

            log.debug("파일 다운로드 테스트 완료");
        }

        @Test
        @DisplayName("존재하지 않는 파일 다운로드 실패")
        void testDownloadFileNotFound() throws Exception {
            log.debug("존재하지 않는 파일 다운로드 테스트 시작");

            mockMvc.perform(get("/api/files/download/non-existent-file.jpg"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("FILE_001"))
                    .andDo(result -> log.debug("존재하지 않는 파일 다운로드 응답: {}", result.getResponse().getContentAsString()));

            log.debug("존재하지 않는 파일 다운로드 테스트 완료");
        }
    }

    @Nested
    @DisplayName("파일 정보 조회 API 테스트")
    class FileInfoApiTests {

        @Test
        @DisplayName("파일 정보 조회 성공")
        void testGetFileInfo() throws Exception {
            log.debug("파일 정보 조회 테스트 시작");

            mockMvc.perform(get("/api/files/" + testFile.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.id").value(testFile.getId()))
                    .andExpect(jsonPath("$.data.originalName").value("test-image.jpg"))
                    .andDo(result -> log.debug("파일 정보 조회 응답: {}", result.getResponse().getContentAsString()));

            log.debug("파일 정보 조회 테스트 완료");
        }

        @Test
        @DisplayName("존재하지 않는 파일 정보 조회 실패")
        void testGetFileInfoNotFound() throws Exception {
            log.debug("존재하지 않는 파일 정보 조회 테스트 시작");

            mockMvc.perform(get("/api/files/999")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("FILE_001"))
                    .andDo(result -> log.debug("존재하지 않는 파일 정보 조회 응답: {}", result.getResponse().getContentAsString()));

            log.debug("존재하지 않는 파일 정보 조회 테스트 완료");
        }

        @Test
        @DisplayName("참조별 파일 목록 조회 성공")
        void testGetFilesByReference() throws Exception {
            log.debug("참조별 파일 목록 조회 테스트 시작");

            // 같은 참조에 추가 파일 업로드
            MockMultipartFile mockFile2 = new MockMultipartFile(
                    "file", "test2.jpg", "image/jpeg", "content2".getBytes()
            );
            fileService.uploadImage(mockFile2, FileType.BOARD, 1L, "testUser");

            mockMvc.perform(get("/api/files/reference")
                            .param("fileType", "BOARD")
                            .param("referenceId", "1")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andDo(result -> log.debug("참조별 파일 목록 조회 응답: {}", result.getResponse().getContentAsString()));

            log.debug("참조별 파일 목록 조회 테스트 완료");
        }

        @Test
        @DisplayName("전체 파일 목록 페이지네이션 조회 성공")
        void testGetAllFiles() throws Exception {
            log.debug("전체 파일 목록 조회 테스트 시작");

            // 추가 파일 업로드
            for (int i = 0; i < 3; i++) {
                MockMultipartFile mockFile = new MockMultipartFile(
                        "file", "test" + i + ".jpg", "image/jpeg", ("content" + i).getBytes()
                );
                fileService.uploadImage(mockFile, FileType.OTHER, null, "testUser");
            }

            mockMvc.perform(get("/api/files")
                            .param("page", "0")
                            .param("size", "2")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content.length()").value(2))
                    .andExpect(jsonPath("$.data.totalElements").value(4))
                    .andExpect(jsonPath("$.data.totalPages").value(2))
                    .andDo(result -> log.debug("전체 파일 목록 조회 응답: {}", result.getResponse().getContentAsString()));

            log.debug("전체 파일 목록 조회 테스트 완료");
        }
    }

    @Nested
    @DisplayName("파일 삭제 API 테스트")
    class FileDeleteApiTests {

        @Test
        @WithMockUser(username = "testUser")
        @DisplayName("본인 파일 삭제 성공")
        void testDeleteFileByOwner() throws Exception {
            log.debug("본인 파일 삭제 테스트 시작");

            Long fileId = testFile.getId();

            mockMvc.perform(delete("/api/files/" + fileId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andExpect(jsonPath("$.message").value("파일 삭제 성공"))
                    .andDo(result -> log.debug("본인 파일 삭제 응답: {}", result.getResponse().getContentAsString()));

            // 삭제 확인
            assertFalse(fileRepository.existsById(fileId));

            log.debug("본인 파일 삭제 테스트 완료");
        }

        @Test
        @WithMockUser(username = "otherUser")
        @DisplayName("타인 파일 삭제 실패")
        void testDeleteFileByOtherUser() throws Exception {
            log.debug("타인 파일 삭제 테스트 시작");

            mockMvc.perform(delete("/api/files/" + testFile.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("AUTH_003"))
                    .andDo(result -> log.debug("타인 파일 삭제 응답: {}", result.getResponse().getContentAsString()));

            // 파일이 삭제되지 않았는지 확인
            assertTrue(fileRepository.existsById(testFile.getId()));

            log.debug("타인 파일 삭제 테스트 완료");
        }

        @Test
        @DisplayName("미인증 사용자 파일 삭제 실패")
        void testDeleteFileUnauthorized() throws Exception {
            log.debug("미인증 사용자 파일 삭제 테스트 시작");

            mockMvc.perform(delete("/api/files/" + testFile.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized())
                    .andDo(result -> log.debug("미인증 사용자 파일 삭제 응답: {}", result.getResponse().getContentAsString()));

            log.debug("미인증 사용자 파일 삭제 테스트 완료");
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("관리자 파일 삭제 성공")
        void testDeleteFileByAdmin() throws Exception {
            log.debug("관리자 파일 삭제 테스트 시작");

            Long fileId = testFile.getId();

            mockMvc.perform(delete("/api/files/admin/" + fileId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andExpect(jsonPath("$.message").value("파일 삭제 성공"))
                    .andDo(result -> log.debug("관리자 파일 삭제 응답: {}", result.getResponse().getContentAsString()));

            // 삭제 확인
            assertFalse(fileRepository.existsById(fileId));

            log.debug("관리자 파일 삭제 테스트 완료");
        }

        @Test
        @WithMockUser(username = "normalUser")
        @DisplayName("일반 사용자 관리자 삭제 API 접근 실패")
        void testDeleteFileByAdminUnauthorized() throws Exception {
            log.debug("일반 사용자 관리자 삭제 API 접근 테스트 시작");

            mockMvc.perform(delete("/api/files/admin/" + testFile.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden())
                    .andDo(result -> log.debug("일반 사용자 관리자 삭제 API 접근 응답: {}", result.getResponse().getContentAsString()));

            // 파일이 삭제되지 않았는지 확인
            assertTrue(fileRepository.existsById(testFile.getId()));

            log.debug("일반 사용자 관리자 삭제 API 접근 테스트 완료");
        }

        @Test
        @WithMockUser(username = "testUser")
        @DisplayName("존재하지 않는 파일 삭제 실패")
        void testDeleteFileNotFound() throws Exception {
            log.debug("존재하지 않는 파일 삭제 테스트 시작");

            mockMvc.perform(delete("/api/files/999")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("FILE_001"))
                    .andDo(result -> log.debug("존재하지 않는 파일 삭제 응답: {}", result.getResponse().getContentAsString()));

            log.debug("존재하지 않는 파일 삭제 테스트 완료");
        }
    }
}
