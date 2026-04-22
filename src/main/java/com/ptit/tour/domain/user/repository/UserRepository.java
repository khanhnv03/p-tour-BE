package com.ptit.tour.domain.user.repository;

import com.ptit.tour.domain.user.entity.User;
import com.ptit.tour.domain.user.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Page<User> findByStatus(UserStatus status, Pageable pageable);
    Page<User> findByFullNameContainingIgnoreCase(String keyword, Pageable pageable);
}
