package com.matchhub.catconnect.domain.share.model.entity;

import com.matchhub.catconnect.common.model.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 게시글 공유 링크 엔티티
 * 게시글 ID와 단축코드를 매핑하여 저장함
 */
@Entity
@Table(name = "tb_share_link")
@Getter
@NoArgsConstructor
public class ShareLink extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long boardId;

    @Column(nullable = false, unique = true, length = 8)
    private String shortCode;

    public ShareLink(Long boardId, String shortCode) {
        this.boardId = boardId;
        this.shortCode = shortCode;
    }
}

