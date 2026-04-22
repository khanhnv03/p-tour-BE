package com.ptit.tour.domain.blog.dto;

import com.ptit.tour.domain.blog.enums.BlockType;
import com.ptit.tour.domain.blog.enums.BlogStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;

public record SaveBlogPostRequest(
    @NotBlank @Size(max = 500) String title,
    @Size(max = 500) String coverImageUrl,
    String excerpt,
    @NotNull BlogStatus status,
    Instant scheduledAt,
    @Valid List<BlockRequest> blocks
) {
    public record BlockRequest(
        @NotNull BlockType blockType,
        String content,
        @Size(max = 500) String imageUrl,
        int sortOrder
    ) {}
}
