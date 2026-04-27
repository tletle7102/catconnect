package com.matchhub.catconnect.domain.report.model.entity;

import com.matchhub.catconnect.common.model.entity.BaseEntity;
import com.matchhub.catconnect.domain.report.model.enums.ReportReason;
import com.matchhub.catconnect.domain.report.model.enums.ReportStatus;
import com.matchhub.catconnect.domain.report.model.enums.ReportTargetType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_report", uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_report_reporter_target",
                columnNames = {"reporter", "target_type", "target_id"}
        )
})
@Getter
@NoArgsConstructor
public class Report extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "신고자는 필수입니다.")
    @Column(nullable = false)
    private String reporter;

    @NotNull(message = "신고 대상 유형은 필수입니다.")
    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private ReportTargetType targetType;

    @NotNull(message = "신고 대상 ID는 필수입니다.")
    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @NotNull(message = "신고 사유는 필수입니다.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportReason reason;

    @Size(max = 500, message = "상세 사유는 500자 이내여야 합니다.")
    @Column(length = 500)
    private String detail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status = ReportStatus.PENDING;

    public Report(String reporter, ReportTargetType targetType, Long targetId, ReportReason reason, String detail) {
        this.reporter = reporter;
        this.targetType = targetType;
        this.targetId = targetId;
        this.reason = reason;
        this.detail = detail;
    }

    public void resolve(ReportStatus status) {
        this.status = status;
    }
}
