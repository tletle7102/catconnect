package com.matchhub.catconnect.domain.report.model.dto;

import com.matchhub.catconnect.domain.report.model.enums.SanctionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SanctionRequestDTO {

    @NotBlank(message = "사용자명은 필수입니다.")
    private String username;

    @NotNull(message = "제재 유형은 필수입니다.")
    private SanctionType sanctionType;

    private String reason;
}
