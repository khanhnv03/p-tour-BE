package com.ptit.tour.domain.wishlist.controller;

import com.ptit.tour.common.response.ApiResponse;
import com.ptit.tour.common.response.PageResponse;
import com.ptit.tour.common.security.UserPrincipal;
import com.ptit.tour.domain.wishlist.dto.WishlistItemDto;
import com.ptit.tour.domain.wishlist.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Wishlist")
@RestController
@RequestMapping("/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @Operation(summary = "Danh sách tour yêu thích của tôi")
    @GetMapping
    public ApiResponse<PageResponse<WishlistItemDto>> getMyWishlist(
        @AuthenticationPrincipal UserPrincipal principal,
        @PageableDefault(size = 12) Pageable pageable) {
        return ApiResponse.ok(PageResponse.of(wishlistService.getMyWishlist(principal.getId(), pageable)));
    }

    @Operation(summary = "Kiểm tra tour có trong wishlist không")
    @GetMapping("/check/{tourId}")
    public ApiResponse<Boolean> check(@AuthenticationPrincipal UserPrincipal principal,
                                       @PathVariable Long tourId) {
        return ApiResponse.ok(wishlistService.isWishlisted(principal.getId(), tourId));
    }

    @Operation(summary = "Thêm tour vào wishlist")
    @PostMapping("/{tourId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<WishlistItemDto> add(@AuthenticationPrincipal UserPrincipal principal,
                                             @PathVariable Long tourId) {
        return ApiResponse.created(wishlistService.add(principal.getId(), tourId));
    }

    @Operation(summary = "Xoá tour khỏi wishlist")
    @DeleteMapping("/{tourId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> remove(@AuthenticationPrincipal UserPrincipal principal,
                                     @PathVariable Long tourId) {
        wishlistService.remove(principal.getId(), tourId);
        return ApiResponse.noContent("Đã xoá khỏi danh sách yêu thích");
    }
}
