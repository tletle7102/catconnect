package com.matchhub.catconnect.domain.share.repository;

import com.matchhub.catconnect.domain.share.model.entity.ShareLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 공유 링크 JPA 레포지토리
 */
public interface ShareLinkRepository extends JpaRepository<ShareLink, Long> {

    /**
     * 단축코드로 공유 링크 조회
     */
    Optional<ShareLink> findByShortCode(String shortCode);

    /**
     * 게시글 ID로 공유 링크 조회
     */
    Optional<ShareLink> findByBoardId(Long boardId);
}
