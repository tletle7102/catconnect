package com.matchhub.catconnect.domain.user.repository;

import com.matchhub.catconnect.domain.user.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Page<User> findAll(Pageable pageable);
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);
}
