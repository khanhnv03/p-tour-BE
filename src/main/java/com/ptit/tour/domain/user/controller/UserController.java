package com.ptit.tour.domain.user.controller;

import com.ptit.tour.common.response.ApiResponse;
import com.ptit.tour.common.response.PageResponse;
import com.ptit.tour.common.security.UserPrincipal;
import com.ptit.tour.domain.user.dto.AdminCustomerDetailDto;
import com.ptit.tour.domain.user.dto.ChangePasswordRequest;
import com.ptit.tour.domain.user.dto.NotificationPreferencesDto;
import com.ptit.tour.domain.user.dto.UpdateProfileRequest;
import com.ptit.tour.domain.user.dto.UserDto;
import com.ptit.tour.domain.user.enums.UserStatus;
import com.ptit.tour.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Users")
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "Lấy thông tin cá nhân")
    @GetMapping("/users/me")
    public ApiResponse<UserDto> getMe(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(userService.getById(principal.getId()));
    }

    @Operation(summary = "Cập nhật thông tin cá nhân")
    @PutMapping("/users/me")
    public ApiResponse<UserDto> updateMe(@AuthenticationPrincipal UserPrincipal principal,
                                          @Valid @RequestBody UpdateProfileRequest request) {
        return ApiResponse.ok(userService.updateProfile(principal.getId(), request));
    }

    @Operation(summary = "Đổi mật khẩu")
    @PutMapping("/users/me/password")
    public ApiResponse<Void> changePassword(@AuthenticationPrincipal UserPrincipal principal,
                                             @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(principal.getId(), request);
        return ApiResponse.ok(null);
    }

    @Operation(summary = "Lấy tuỳ chọn thông báo")
    @GetMapping("/users/me/notification-preferences")
    public ApiResponse<NotificationPreferencesDto> getNotificationPreferences(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(userService.getNotificationPreferences(principal.getId()));
    }

    @Operation(summary = "Cập nhật tuỳ chọn thông báo")
    @PutMapping("/users/me/notification-preferences")
    public ApiResponse<NotificationPreferencesDto> updateNotificationPreferences(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody NotificationPreferencesDto dto) {
        return ApiResponse.ok(userService.updateNotificationPreferences(principal.getId(), dto));
    }

    // ── Admin ────────────────────────────────────────────────────────────────

    @Operation(summary = "[Admin] Danh sách người dùng")
    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageResponse<UserDto>> listUsers(
        @RequestParam(required = false) String keyword,
        @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.ok(PageResponse.of(userService.findAll(keyword, pageable)));
    }

    @Operation(summary = "[Admin] Xem thông tin người dùng")
    @GetMapping("/admin/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserDto> getUserById(@PathVariable Long id) {
        return ApiResponse.ok(userService.getById(id));
    }

    @Operation(summary = "[Admin] Chi tiết khách hàng kèm thống kê booking")
    @GetMapping("/admin/users/{id}/detail")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<AdminCustomerDetailDto> getCustomerDetail(@PathVariable Long id) {
        return ApiResponse.ok(userService.getCustomerDetail(id));
    }

    @Operation(summary = "[Admin] Cập nhật thông tin người dùng")
    @PutMapping("/admin/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserDto> updateUser(@PathVariable Long id,
                                           @Valid @RequestBody UpdateProfileRequest request) {
        return ApiResponse.ok(userService.updateAdmin(id, request));
    }

    @Operation(summary = "[Admin] Khoá / mở khoá người dùng")
    @PatchMapping("/admin/users/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserDto> updateStatus(@PathVariable Long id,
                                              @RequestParam UserStatus status) {
        return ApiResponse.ok(userService.updateStatus(id, status));
    }
}
