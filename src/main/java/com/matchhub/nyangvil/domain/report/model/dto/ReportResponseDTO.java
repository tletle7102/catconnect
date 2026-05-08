package com.matchhub.nyangvil.domain.report.model.dto;

import com.matchhub.nyangvil.domain.report.model.enums.ReportReason;
import com.matchhub.nyangvil.domain.report.model.enums.ReportStatus;
import com.matchhub.nyangvil.domain.report.model.enums.ReportTargetType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ReportResponseDTO {
    private Long id;
    private String reporter;
    private ReportTargetType targetType;
    private Long targetId;
    private ReportReason reason;
    private String detail;
    private ReportStatus status;
    private LocalDateTime createdDttm;
    private LocalDateTime updatedDttm;
}
