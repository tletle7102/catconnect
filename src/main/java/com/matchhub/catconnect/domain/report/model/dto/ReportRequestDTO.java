package com.matchhub.catconnect.domain.report.model.dto;

import com.matchhub.catconnect.domain.report.model.enums.ReportReason;
import com.matchhub.catconnect.domain.report.model.enums.ReportTargetType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportRequestDTO {

    @NotNull(message = "신고 대상 유형은 필수입니다.")
    private ReportTargetType targetType;

    @NotNull(message = "신고 대상 ID는 필수입니다.")
    private Long targetId;

    @NotNull(message = "신고 사유는 필수입니다.")
    private ReportReason reason;

    @Size(max = 500, message = "상세 사유는 500자 이내여야 합니다.")
    private String detail;
}
