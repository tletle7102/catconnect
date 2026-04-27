package com.matchhub.catconnect.domain.report.service;

import com.matchhub.catconnect.domain.board.model.entity.Board;
import com.matchhub.catconnect.domain.board.repository.BoardRepository;
import com.matchhub.catconnect.domain.comment.model.entity.Comment;
import com.matchhub.catconnect.domain.comment.repository.CommentRepository;
import com.matchhub.catconnect.domain.report.model.dto.ReportResponseDTO;
import com.matchhub.catconnect.domain.report.model.entity.Report;
import com.matchhub.catconnect.domain.report.model.entity.UserSanction;
import com.matchhub.catconnect.domain.report.model.enums.*;
import com.matchhub.catconnect.domain.report.repository.ReportRepository;
import com.matchhub.catconnect.domain.report.repository.UserSanctionRepository;
import com.matchhub.catconnect.global.exception.AppException;
import com.matchhub.catconnect.global.exception.Domain;
import com.matchhub.catconnect.global.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportService.class);
    private static final int AUTO_BLIND_THRESHOLD = 5;

    private final ReportRepository reportRepository;
    private final UserSanctionRepository userSanctionRepository;
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;

    public ReportService(ReportRepository reportRepository,
                         UserSanctionRepository userSanctionRepository,
                         BoardRepository boardRepository,
                         CommentRepository commentRepository) {
        this.reportRepository = reportRepository;
        this.userSanctionRepository = userSanctionRepository;
        this.boardRepository = boardRepository;
        this.commentRepository = commentRepository;
    }

    /**
     * 신고 생성
     */
    public ReportResponseDTO createReport(String reporter, ReportTargetType targetType, Long targetId,
                                           ReportReason reason, String detail) {
        log.debug("신고 생성 요청: reporter={}, targetType={}, targetId={}, reason={}", reporter, targetType, targetId, reason);

        // 대상 존재 여부 확인 및 자기 자신 신고 방지
        String targetAuthor = getTargetAuthor(targetType, targetId);
        if (reporter.equals(targetAuthor)) {
            log.warn("자신의 콘텐츠 신고 시도: reporter={}", reporter);
            throw new AppException(Domain.REPORT, ErrorCode.INVALID_REQUEST, "자신의 콘텐츠는 신고할 수 없습니다.");
        }

        // 중복 신고 검증
        if (reportRepository.existsByReporterAndTargetTypeAndTargetId(reporter, targetType, targetId)) {
            log.warn("중복 신고 시도: reporter={}, targetType={}, targetId={}", reporter, targetType, targetId);
            throw new AppException(Domain.REPORT, ErrorCode.REPORT_ALREADY_EXISTS);
        }

        // 신고 저장
        Report report = new Report(reporter, targetType, targetId, reason, detail);
        reportRepository.save(report);
        log.debug("신고 생성 완료: id={}", report.getId());

        // 누적 신고 수 확인 후 자동 블라인드 처리
        long reportCount = reportRepository.countByTargetTypeAndTargetId(targetType, targetId);
        if (reportCount >= AUTO_BLIND_THRESHOLD) {
            autoBlindTarget(targetType, targetId);
        }

        return toResponseDTO(report);
    }

    /**
     * 신고 처리 (수락/거절)
     */
    public ReportResponseDTO resolveReport(Long reportId, ReportStatus status) {
        log.debug("신고 처리 요청: reportId={}, status={}", reportId, status);

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new AppException(Domain.REPORT, ErrorCode.REPORT_NOT_FOUND));

        report.resolve(status);
        reportRepository.save(report);
        log.debug("신고 처리 완료: reportId={}, status={}", reportId, status);

        return toResponseDTO(report);
    }

    /**
     * 대기 중인 신고 목록 조회 (페이지네이션)
     */
    @Transactional(readOnly = true)
    public Page<ReportResponseDTO> getReportsPending(int page, int size) {
        log.debug("대기 중인 신고 목록 조회: page={}, size={}", page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDttm").descending());
        Page<Report> reportPage = reportRepository.findByStatus(ReportStatus.PENDING, pageable);
        return reportPage.map(this::toResponseDTO);
    }

    /**
     * 전체 신고 목록 조회 (페이지네이션)
     */
    @Transactional(readOnly = true)
    public Page<ReportResponseDTO> getAllReports(int page, int size) {
        log.debug("전체 신고 목록 조회: page={}, size={}", page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDttm").descending());
        Page<Report> reportPage = reportRepository.findAll(pageable);
        return reportPage.map(this::toResponseDTO);
    }

    /**
     * 상태별 신고 목록 조회 (페이지네이션)
     */
    @Transactional(readOnly = true)
    public Page<ReportResponseDTO> getReportsByStatus(ReportStatus status, int page, int size) {
        log.debug("상태별 신고 목록 조회: status={}, page={}, size={}", status, page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDttm").descending());
        Page<Report> reportPage = reportRepository.findByStatus(status, pageable);
        return reportPage.map(this::toResponseDTO);
    }

    /**
     * 제재 적용
     */
    public void applySanction(String username, SanctionType type, String reason) {
        log.debug("제재 적용 요청: username={}, type={}, reason={}", username, type, reason);

        LocalDateTime expiresAt = null;
        if (type == SanctionType.POST_BAN_TEMP) {
            expiresAt = LocalDateTime.now().plusDays(7); // 7일 작성 정지
        }
        // WARNING, POST_BAN_PERMANENT, ACCOUNT_BAN은 expiresAt이 null (영구)

        UserSanction sanction = new UserSanction(username, type, reason, expiresAt);
        userSanctionRepository.save(sanction);
        log.debug("제재 적용 완료: username={}, type={}", username, type);

        // ACCOUNT_BAN인 경우 추가 처리 (해당 사용자의 모든 게시글/댓글 블라인드 처리 등)
        if (type == SanctionType.ACCOUNT_BAN) {
            log.debug("계정 정지 사용자 콘텐츠 블라인드 처리: username={}", username);
            // 추후 사용자 soft delete 및 게시글 익명화 로직 추가 가능
        }
    }

    /**
     * 사용자의 게시글 작성 제한 여부 확인
     */
    @Transactional(readOnly = true)
    public boolean isUserPostBanned(String username) {
        log.debug("게시글 작성 제한 확인: username={}", username);
        List<UserSanction> sanctions = userSanctionRepository.findByUsername(username);
        return sanctions.stream()
                .filter(s -> s.getSanctionType() == SanctionType.POST_BAN_TEMP
                        || s.getSanctionType() == SanctionType.POST_BAN_PERMANENT
                        || s.getSanctionType() == SanctionType.ACCOUNT_BAN)
                .anyMatch(UserSanction::isActive);
    }

    /**
     * 신고 대상의 작성자를 조회
     */
    private String getTargetAuthor(ReportTargetType targetType, Long targetId) {
        if (targetType == ReportTargetType.BOARD) {
            Board board = boardRepository.findById(targetId)
                    .orElseThrow(() -> new AppException(Domain.BOARD, ErrorCode.BOARD_NOT_FOUND));
            return board.getAuthor();
        } else {
            Comment comment = commentRepository.findById(targetId)
                    .orElseThrow(() -> new AppException(Domain.COMMENT, ErrorCode.COMMENT_NOT_FOUND));
            return comment.getAuthor();
        }
    }

    /**
     * 자동 블라인드 처리 (신고 누적 5건 이상)
     */
    private void autoBlindTarget(ReportTargetType targetType, Long targetId) {
        log.debug("자동 블라인드 처리: targetType={}, targetId={}", targetType, targetId);
        if (targetType == ReportTargetType.BOARD) {
            boardRepository.findById(targetId).ifPresent(board -> {
                board.blind();
                boardRepository.save(board);
                log.debug("게시글 블라인드 처리 완료: boardId={}", targetId);
            });
        } else {
            commentRepository.findById(targetId).ifPresent(comment -> {
                comment.blind();
                commentRepository.save(comment);
                log.debug("댓글 블라인드 처리 완료: commentId={}", targetId);
            });
        }
    }

    /**
     * Report -> ReportResponseDTO 변환
     */
    private ReportResponseDTO toResponseDTO(Report report) {
        ReportResponseDTO dto = new ReportResponseDTO();
        dto.setId(report.getId());
        dto.setReporter(report.getReporter());
        dto.setTargetType(report.getTargetType());
        dto.setTargetId(report.getTargetId());
        dto.setReason(report.getReason());
        dto.setDetail(report.getDetail());
        dto.setStatus(report.getStatus());
        dto.setCreatedDttm(report.getCreatedDttm());
        dto.setUpdatedDttm(report.getUpdatedDttm());
        return dto;
    }
}
