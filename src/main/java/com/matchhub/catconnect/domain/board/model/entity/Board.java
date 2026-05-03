package com.matchhub.catconnect.domain.board.model.entity;

import com.matchhub.catconnect.common.model.entity.BaseEntity;
import com.matchhub.catconnect.domain.board.model.enums.BoardCategory;
import com.matchhub.catconnect.domain.board.model.enums.BoardPermissionLevel;
import com.matchhub.catconnect.domain.comment.model.entity.Comment;
import com.matchhub.catconnect.domain.like.model.entity.Like;
import com.matchhub.catconnect.global.validation.RestrictedString;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_board")
@Getter
@Setter
@NoArgsConstructor
public class Board extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 100, message = "제목은 100자 이내여야 합니다.")
    @RestrictedString
    @Column(nullable = false)
    private String title;

    @NotBlank(message = "내용은 필수입니다.")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @NotBlank(message = "작성자는 필수입니다.")
    @Column(nullable = false)
    private String author;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BoardCategory category = BoardCategory.FREE;

    @Column(nullable = false)
    private int viewCount = 0;

    @Column(nullable = false)
    private boolean blinded = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BoardPermissionLevel readPermission = BoardPermissionLevel.ANYONE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BoardPermissionLevel writePermission = BoardPermissionLevel.MEMBER;

    @Column(nullable = false)
    private boolean ownerReadOnly = false;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Like> likes = new ArrayList<>();

    public Board(String title, String content, String author) {
        this.title = title;
        this.content = content;
        this.author = author;
    }

    public Board(String title, String content, String author, BoardCategory category) {
        this.title = title;
        this.content = content;
        this.author = author;
        this.category = category;
    }

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public void incrementViewCount() {
        this.viewCount++;
    }

    public void blind() {
        this.blinded = true;
    }
}
