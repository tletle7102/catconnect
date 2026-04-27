package com.matchhub.catconnect.domain.report.model.dto;

import com.matchhub.catconnect.domain.report.model.enums.ReportStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportResolveRequestDTO {

    @NotNull(message = "처리 상태는 필수입니다.")
    private ReportStatus status;
}
