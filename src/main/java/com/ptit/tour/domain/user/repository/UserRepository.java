package com.ptit.tour.domain.user.repository;

import com.ptit.tour.domain.user.entity.User;
import com.ptit.tour.domain.user.enums.UserRole;
import com.ptit.tour.domain.user.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Page<User> findByStatus(UserStatus status, Pageable pageable);
    Page<User> findByFullNameContainingIgnoreCase(String keyword, Pageable pageable);
    long countByRole(UserRole role);
    long countByRoleAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(UserRole role, Instant from, Instant to);
    long countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(Instant from, Instant to);

    @Query("""
        SELECT u FROM User u
        WHERE (:keyword IS NULL
          OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
          OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
          OR u.phone LIKE CONCAT('%', :keyword, '%'))
        """)
    Page<User> searchAdmin(@Param("keyword") String keyword, Pageable pageable);
}
