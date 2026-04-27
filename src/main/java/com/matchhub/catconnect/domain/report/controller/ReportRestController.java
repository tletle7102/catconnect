package com.matchhub.catconnect.domain.report.controller;

import com.matchhub.catconnect.domain.report.model.dto.ReportRequestDTO;
import com.matchhub.catconnect.domain.report.model.dto.ReportResponseDTO;
import com.matchhub.catconnect.domain.report.service.ReportService;
import com.matchhub.catconnect.global.exception.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "신고 API", description = "일반 사용자 신고 관련 REST API")
@RestController
@RequestMapping("/api/reports")
public class ReportRestController {

    private static final Logger log = LoggerFactory.getLogger(ReportRestController.class);
    private final ReportService reportService;

    public ReportRestController(ReportService reportService) {
        this.reportService = reportService;
    }

    @Operation(summary = "신고 등록", description = "게시글 또는 댓글을 신고합니다.")
    @PostMapping
    public ResponseEntity<Response<ReportResponseDTO>> createReport(
            @Valid @RequestBody ReportRequestDTO requestDTO,
            Authentication authentication
    ) {
        String reporter = authentication.getName();
        log.debug("POST /api/reports 요청: reporter={}, targetType={}, targetId={}",
                reporter, requestDTO.getTargetType(), requestDTO.getTargetId());

        ReportResponseDTO report = reportService.createReport(
                reporter,
                requestDTO.getTargetType(),
                requestDTO.getTargetId(),
                requestDTO.getReason(),
                requestDTO.getDetail()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Response.success(report, "신고가 접수되었습니다."));
    }
}
