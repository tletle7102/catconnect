package com.matchhub.catconnect.domain.user.repository;

import com.matchhub.catconnect.domain.user.model.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
}
