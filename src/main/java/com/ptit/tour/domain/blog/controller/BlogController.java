package com.ptit.tour.domain.blog.controller;

import com.ptit.tour.common.response.ApiResponse;
import com.ptit.tour.common.response.PageResponse;
import com.ptit.tour.common.security.UserPrincipal;
import com.ptit.tour.domain.blog.dto.BlogPostDto;
import com.ptit.tour.domain.blog.dto.BlogPostSummaryDto;
import com.ptit.tour.domain.blog.dto.SaveBlogPostRequest;
import com.ptit.tour.domain.blog.service.BlogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Blog / Journal")
@RestController
@RequiredArgsConstructor
public class BlogController {

    private final BlogService blogService;

    @Operation(summary = "Danh sách bài viết đã xuất bản")
    @GetMapping("/blog")
    public ApiResponse<PageResponse<BlogPostSummaryDto>> list(
        @RequestParam(required = false) String keyword,
        @PageableDefault(size = 10) Pageable pageable) {
        return ApiResponse.ok(PageResponse.of(blogService.listPublished(keyword, pageable)));
    }

    @Operation(summary = "Chi tiết bài viết theo slug")
    @GetMapping("/blog/{slug}")
    public ApiResponse<BlogPostDto> getBySlug(@PathVariable String slug) {
        return ApiResponse.ok(blogService.getBySlug(slug));
    }

    // ── Admin ────────────────────────────────────────────────────────────────

    @Operation(summary = "[Admin] Tất cả bài viết")
    @GetMapping("/admin/blog")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageResponse<BlogPostSummaryDto>> listAll(
        @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.ok(PageResponse.of(blogService.findAll(pageable)));
    }

    @Operation(summary = "[Admin] Chi tiết bài viết theo ID")
    @GetMapping("/admin/blog/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<BlogPostDto> getById(@PathVariable Long id) {
        return ApiResponse.ok(blogService.getById(id));
    }

    @Operation(summary = "[Admin] Tạo bài viết")
    @PostMapping("/admin/blog")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<BlogPostDto> create(@AuthenticationPrincipal UserPrincipal principal,
                                            @Valid @RequestBody SaveBlogPostRequest request) {
        return ApiResponse.created(blogService.create(principal.getId(), request));
    }

    @Operation(summary = "[Admin] Cập nhật bài viết")
    @PutMapping("/admin/blog/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<BlogPostDto> update(@PathVariable Long id,
                                            @Valid @RequestBody SaveBlogPostRequest request) {
        return ApiResponse.ok(blogService.update(id, request));
    }

    @Operation(summary = "[Admin] Xoá bài viết")
    @DeleteMapping("/admin/blog/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        blogService.delete(id);
        return ApiResponse.noContent("Đã xoá bài viết");
    }
}
