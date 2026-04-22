package com.ptit.tour.domain.blog.repository;

import com.ptit.tour.domain.blog.entity.BlogPost;
import com.ptit.tour.domain.blog.enums.BlogStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {

    Optional<BlogPost> findBySlug(String slug);
    boolean existsBySlug(String slug);
    Page<BlogPost> findByStatus(BlogStatus status, Pageable pageable);

    /** Find scheduled posts whose publish time has arrived. */
    @Query("SELECT p FROM BlogPost p WHERE p.status = 'SCHEDULED' AND p.scheduledAt <= :now")
    List<BlogPost> findDueScheduledPosts(Instant now);

    Page<BlogPost> findByTitleContainingIgnoreCaseAndStatus(String keyword, BlogStatus status, Pageable pageable);
}
