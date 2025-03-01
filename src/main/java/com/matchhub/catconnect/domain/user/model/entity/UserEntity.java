package com.matchhub.catconnect.domain.user.model.entity;

import com.matchhub.catconnect.common.model.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_user")
@Getter
@NoArgsConstructor
public class UserEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;
    private String nickname;

    //폼 데이터를 받는 용도의 생성자
    public UserEntity(String username, String password, String nickname) {

        this.username = username;
        this.password = password;
        this.nickname = nickname;

    }



}
