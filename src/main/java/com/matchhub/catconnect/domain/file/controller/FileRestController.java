package com.matchhub.catconnect.domain.file.controller;

import com.matchhub.catconnect.domain.file.model.dto.FileResponseDTO;
import com.matchhub.catconnect.domain.file.model.enums.FileType;
import com.matchhub.catconnect.domain.file.service.FileService;
import com.matchhub.catconnect.global.exception.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 파일 업로드/다운로드 REST API 컨트롤러
 */
@Tag(name = "파일 API", description = "파일 업로드/다운로드 관련 REST API")
@RestController
@RequestMapping("/api/files")
public class FileRestController {

    private static final Logger log = LoggerFactory.getLogger(FileRestController.class);
    private final FileService fileService;

    public FileRestController(FileService fileService) {
        this.fileService = fileService;
    }

    @Operation(summary = "이미지 업로드", description = "이미지 파일을 업로드합니다")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Response<FileResponseDTO>> uploadImage(
            @Parameter(description = "업로드할 이미지 파일")
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "파일 타입 (PROFILE, BOARD, OTHER)")
            @RequestParam(defaultValue = "OTHER") FileType fileType,
            @Parameter(description = "참조 ID (게시글 ID, 사용자 ID 등)")
            @RequestParam(required = false) Long referenceId,
            Authentication authentication) {
        log.debug("POST /api/files/upload 요청: fileType={}, referenceId={}", fileType, referenceId);
        String username = authentication.getName();
        FileResponseDTO response = fileService.uploadImage(file, fileType, referenceId, username);
        return ResponseEntity.ok(Response.success(response, "파일 업로드 성공"));
    }

    @Operation(summary = "파일 다운로드", description = "파일을 다운로드합니다")
    @GetMapping("/download/{storedName}")
    public ResponseEntity<Resource> downloadFile(
            @Parameter(description = "저장된 파일명")
            @PathVariable String storedName) {
        log.debug("GET /api/files/download/{} 요청", storedName);

        Resource resource = fileService.downloadFile(storedName);
        FileResponseDTO fileInfo = fileService.getFileInfoByStoredName(storedName);

        String encodedFileName = URLEncoder.encode(fileInfo.getOriginalName(), StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(fileInfo.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename*=UTF-8''" + encodedFileName)
                .body(resource);
    }

    @Operation(summary = "파일 정보 조회", description = "파일 정보를 조회합니다")
    @GetMapping("/{fileId}")
    public ResponseEntity<Response<FileResponseDTO>> getFileInfo(
            @Parameter(description = "파일 ID")
            @PathVariable Long fileId) {
        log.debug("GET /api/files/{} 요청", fileId);
        FileResponseDTO response = fileService.getFileInfo(fileId);
        return ResponseEntity.ok(Response.success(response, "파일 정보 조회 성공"));
    }

    @Operation(summary = "참조별 파일 목록 조회", description = "특정 참조(게시글, 사용자 등)에 연결된 파일 목록을 조회합니다")
    @GetMapping("/reference")
    public ResponseEntity<Response<List<FileResponseDTO>>> getFilesByReference(
            @Parameter(description = "파일 타입")
            @RequestParam FileType fileType,
            @Parameter(description = "참조 ID")
            @RequestParam Long referenceId) {
        log.debug("GET /api/files/reference 요청: fileType={}, referenceId={}", fileType, referenceId);
        List<FileResponseDTO> response = fileService.getFilesByReference(fileType, referenceId);
        return ResponseEntity.ok(Response.success(response, "파일 목록 조회 성공"));
    }

    @Operation(summary = "전체 파일 조회", description = "전체 파일 목록을 페이지네이션하여 조회합니다")
    @GetMapping
    public ResponseEntity<Response<Page<FileResponseDTO>>> getAllFiles(
            @Parameter(description = "페이지 번호 (0부터 시작)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기")
            @RequestParam(defaultValue = "10") int size) {
        log.debug("GET /api/files 요청: page={}, size={}", page, size);
        Page<FileResponseDTO> response = fileService.getAllFiles(page, size);
        return ResponseEntity.ok(Response.success(response, "파일 목록 조회 성공"));
    }

    @Operation(summary = "파일 삭제", description = "파일을 삭제합니다. 본인이 업로드한 파일만 삭제할 수 있습니다")
    @DeleteMapping("/{fileId}")
    public ResponseEntity<Response<Void>> deleteFile(
            @Parameter(description = "삭제할 파일 ID")
            @PathVariable Long fileId,
            Authentication authentication) {
        log.debug("DELETE /api/files/{} 요청", fileId);
        String username = authentication.getName();
        fileService.deleteFile(fileId, username);
        return ResponseEntity.ok(Response.success(null, "파일 삭제 성공"));
    }

    @Operation(summary = "파일 삭제 (관리자)", description = "관리자 권한으로 파일을 삭제합니다")
    @DeleteMapping("/admin/{fileId}")
    public ResponseEntity<Response<Void>> deleteFileByAdmin(
            @Parameter(description = "삭제할 파일 ID")
            @PathVariable Long fileId) {
        log.debug("DELETE /api/files/admin/{} 요청", fileId);
        fileService.deleteFileByAdmin(fileId);
        return ResponseEntity.ok(Response.success(null, "파일 삭제 성공"));
    }
}
