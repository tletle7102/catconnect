package com.matchhub.catconnect.domain.board.model.entity;

import com.matchhub.catconnect.common.model.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_board")
@Getter
@NoArgsConstructor
public class BoardEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String content;
    private String location;
    private Long userId;

    public BoardEntity(String title, String content, String location, Long userId) {

        this.title =title;
        this.content = content;
        this.location =location;
        this.userId =userId;

    }



}
