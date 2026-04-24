package com.ptit.tour.domain.newsletter.controller;

import com.ptit.tour.common.response.ApiResponse;
import com.ptit.tour.common.response.PageResponse;
import com.ptit.tour.domain.newsletter.dto.NewsletterSubscriptionDto;
import com.ptit.tour.domain.newsletter.dto.NewsletterSubscriptionRequest;
import com.ptit.tour.domain.newsletter.service.NewsletterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Newsletter")
@RestController
@RequiredArgsConstructor
public class NewsletterController {

    private final NewsletterService newsletterService;

    @Operation(summary = "Đăng ký nhận newsletter")
    @PostMapping("/newsletter/subscriptions")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<NewsletterSubscriptionDto> subscribe(@Valid @RequestBody NewsletterSubscriptionRequest request) {
        return ApiResponse.created(newsletterService.subscribe(request));
    }

    @Operation(summary = "[Admin] Danh sách newsletter subscriptions")
    @GetMapping("/admin/newsletter/subscriptions")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageResponse<NewsletterSubscriptionDto>> list(@PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.ok(PageResponse.of(newsletterService.list(pageable)));
    }
}
