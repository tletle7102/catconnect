package com.matchhub.catconnect.domain.report.repository;

import com.matchhub.catconnect.domain.report.model.entity.UserSanction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserSanctionRepository extends JpaRepository<UserSanction, Long> {

    List<UserSanction> findByUsername(String username);
}
