package com.matchhub.catconnect.domain.report.repository;

import com.matchhub.catconnect.domain.report.model.entity.Report;
import com.matchhub.catconnect.domain.report.model.enums.ReportStatus;
import com.matchhub.catconnect.domain.report.model.enums.ReportTargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    boolean existsByReporterAndTargetTypeAndTargetId(String reporter, ReportTargetType targetType, Long targetId);

    long countByTargetTypeAndTargetId(ReportTargetType targetType, Long targetId);

    Page<Report> findByStatus(ReportStatus status, Pageable pageable);

    Page<Report> findAll(Pageable pageable);

    List<Report> findByTargetTypeAndTargetId(ReportTargetType targetType, Long targetId);
}
