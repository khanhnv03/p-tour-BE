package com.ptit.tour.domain.blog.service;

import com.ptit.tour.domain.blog.dto.BlogPostDto;
import com.ptit.tour.domain.blog.dto.BlogPostSummaryDto;
import com.ptit.tour.domain.blog.dto.SaveBlogPostRequest;
import com.ptit.tour.domain.blog.enums.BlogStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BlogService {
    Page<BlogPostSummaryDto> listPublished(String keyword, Pageable pageable);
    BlogPostDto getBySlug(String slug);
    BlogPostDto getById(Long id);
    // Admin
    Page<BlogPostSummaryDto> findAll(Pageable pageable);
    Page<BlogPostSummaryDto> searchAdmin(BlogStatus status, String keyword, Pageable pageable);
    BlogPostDto create(Long authorId, SaveBlogPostRequest request);
    BlogPostDto update(Long id, SaveBlogPostRequest request);
    void delete(Long id);
    /** Cron: auto-publish scheduled posts. */
    void publishScheduled();
}
