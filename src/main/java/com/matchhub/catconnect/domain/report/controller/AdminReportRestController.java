package com.matchhub.catconnect.domain.report.controller;

import com.matchhub.catconnect.domain.report.model.dto.ReportResolveRequestDTO;
import com.matchhub.catconnect.domain.report.model.dto.ReportResponseDTO;
import com.matchhub.catconnect.domain.report.model.dto.SanctionRequestDTO;
import com.matchhub.catconnect.domain.report.model.enums.ReportStatus;
import com.matchhub.catconnect.domain.report.service.ReportService;
import com.matchhub.catconnect.global.exception.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "관리자 신고 관리 API", description = "관리자 전용 신고 관리 REST API")
@RestController
@RequestMapping("/api/admin/reports")
public class AdminReportRestController {

    private static final Logger log = LoggerFactory.getLogger(AdminReportRestController.class);
    private final ReportService reportService;

    public AdminReportRestController(ReportService reportService) {
        this.reportService = reportService;
    }

    @Operation(summary = "신고 목록 조회", description = "신고 목록을 페이지네이션하여 조회합니다. 상태별 필터링이 가능합니다.")
    @GetMapping
    public ResponseEntity<Response<Page<ReportResponseDTO>>> getReports(
            @Parameter(description = "신고 상태 필터 (PENDING, ACCEPTED, REJECTED)") @RequestParam(required = false) ReportStatus status,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size
    ) {
        log.debug("GET /api/admin/reports 요청: status={}, page={}, size={}", status, page, size);

        Page<ReportResponseDTO> reports;
        if (status != null) {
            reports = reportService.getReportsByStatus(status, page, size);
        } else {
            reports = reportService.getAllReports(page, size);
        }

        return ResponseEntity.ok(Response.success(reports, "신고 목록 조회 성공"));
    }

    @Operation(summary = "신고 처리", description = "신고를 수락 또는 거절 처리합니다.")
    @PutMapping("/{id}/resolve")
    public ResponseEntity<Response<ReportResponseDTO>> resolveReport(
            @Parameter(description = "신고 ID", required = true) @PathVariable Long id,
            @Valid @RequestBody ReportResolveRequestDTO requestDTO
    ) {
        log.debug("PUT /api/admin/reports/{}/resolve 요청: status={}", id, requestDTO.getStatus());

        ReportResponseDTO report = reportService.resolveReport(id, requestDTO.getStatus());
        return ResponseEntity.ok(Response.success(report, "신고 처리 완료"));
    }

    @Operation(summary = "제재 적용", description = "사용자에게 제재를 적용합니다.")
    @PostMapping("/sanction")
    public ResponseEntity<Response<Void>> applySanction(
            @Valid @RequestBody SanctionRequestDTO requestDTO
    ) {
        log.debug("POST /api/admin/reports/sanction 요청: username={}, type={}",
                requestDTO.getUsername(), requestDTO.getSanctionType());

        reportService.applySanction(
                requestDTO.getUsername(),
                requestDTO.getSanctionType(),
                requestDTO.getReason()
        );

        return ResponseEntity.ok(Response.success(null, "제재가 적용되었습니다."));
    }
}
