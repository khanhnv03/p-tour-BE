package com.ptit.tour.auth;

import com.ptit.tour.auth.dto.AuthResponse;
import com.ptit.tour.auth.dto.ForgotPasswordRequest;
import com.ptit.tour.auth.dto.LoginRequest;
import com.ptit.tour.auth.dto.RegisterRequest;
import com.ptit.tour.auth.dto.ResetPasswordRequest;
import com.ptit.tour.auth.dto.UserInfoResponse;
import com.ptit.tour.common.response.ApiResponse;
import com.ptit.tour.common.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "Đăng ký & đăng nhập")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Đăng nhập")
    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    @Operation(summary = "Đăng ký tài khoản mới")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.created(authService.register(request));
    }

    @Operation(summary = "Lấy thông tin người dùng hiện tại")
    @GetMapping("/me")
    public ApiResponse<UserInfoResponse> me(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(authService.me(principal.getId()));
    }

    @Operation(summary = "Yêu cầu đặt lại mật khẩu")
    @PostMapping("/forgot-password")
    public ApiResponse<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ApiResponse.ok(null);
    }

    @Operation(summary = "Đặt lại mật khẩu bằng token")
    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ApiResponse.ok(null);
    }
}
