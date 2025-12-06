package com.matchhub.catconnect.domain.comment.model.entity;

import com.matchhub.catconnect.common.model.entity.BaseEntity;
import com.matchhub.catconnect.domain.board.model.entity.Board;
import com.matchhub.catconnect.global.validation.RestrictedString;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_comment")
@Getter
@NoArgsConstructor
public class Comment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "댓글 내용은 필수입니다.")
    @Size(max = 500, message = "댓글은 500자 이내여야 합니다.")
    @RestrictedString
    @Column(nullable = false)
    private String content;

    @NotBlank(message = "작성자는 필수입니다.")
    @Column(nullable = false)
    private String author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name ="board_id", nullable = false)
    private Board board;

    public Comment(String content, String author, Board board) {
        this.content = content;
        this.author = author;
        this.board = board;
    }
}
