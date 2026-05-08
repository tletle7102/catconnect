package com.matchhub.nyangvil.domain.report.repository;

import com.matchhub.nyangvil.domain.report.model.entity.UserSanction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserSanctionRepository extends JpaRepository<UserSanction, Long> {

    List<UserSanction> findByUsername(String username);
}
