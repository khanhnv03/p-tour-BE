package com.ptit.tour.domain.blog.dto;

import com.ptit.tour.domain.blog.entity.BlogBlock;
import com.ptit.tour.domain.blog.entity.BlogBlockImage;
import com.ptit.tour.domain.blog.entity.BlogPost;
import com.ptit.tour.domain.blog.enums.BlockType;
import com.ptit.tour.domain.blog.enums.BlogStatus;

import java.time.Instant;
import java.util.List;

public record BlogPostDto(
    Long id,
    String title,
    String slug,
    String coverImageUrl,
    String excerpt,
    Long authorId,
    String authorName,
    BlogStatus status,
    Instant publishedAt,
    Instant scheduledAt,
    List<BlockDto> blocks
) {
    public static BlogPostDto from(BlogPost p) {
        return new BlogPostDto(
            p.getId(), p.getTitle(), p.getSlug(), p.getCoverImageUrl(), p.getExcerpt(),
            p.getAuthor().getId(), p.getAuthor().getFullName(),
            p.getStatus(), p.getPublishedAt(), p.getScheduledAt(),
            p.getBlocks().stream().map(BlockDto::from).toList()
        );
    }

    public record BlockDto(Long id, BlockType blockType, String content, String imageUrl, int sortOrder,
                           List<BlockImageDto> images) {
        public static BlockDto from(BlogBlock b) {
            return new BlockDto(b.getId(), b.getBlockType(), b.getContent(), b.getImageUrl(), b.getSortOrder(),
                b.getImages().stream().map(BlockImageDto::from).toList());
        }
    }

    public record BlockImageDto(Long id, String imageUrl, String altText, int sortOrder) {
        public static BlockImageDto from(BlogBlockImage image) {
            return new BlockImageDto(image.getId(), image.getImageUrl(), image.getAltText(), image.getSortOrder());
        }
    }
}
