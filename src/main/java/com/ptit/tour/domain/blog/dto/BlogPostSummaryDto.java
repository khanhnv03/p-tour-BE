package com.ptit.tour.domain.blog.dto;

import com.ptit.tour.domain.blog.entity.BlogPost;
import com.ptit.tour.domain.blog.enums.BlogStatus;

import java.time.Instant;

public record BlogPostSummaryDto(
    Long id,
    String title,
    String slug,
    String coverImageUrl,
    String excerpt,
    String authorName,
    BlogStatus status,
    Instant publishedAt
) {
    public static BlogPostSummaryDto from(BlogPost p) {
        return new BlogPostSummaryDto(p.getId(), p.getTitle(), p.getSlug(),
            p.getCoverImageUrl(), p.getExcerpt(), p.getAuthor().getFullName(),
            p.getStatus(), p.getPublishedAt());
    }
}
