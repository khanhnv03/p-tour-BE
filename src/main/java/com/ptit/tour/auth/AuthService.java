package com.ptit.tour.auth;

import com.ptit.tour.auth.dto.AuthResponse;
import com.ptit.tour.auth.dto.LoginRequest;
import com.ptit.tour.auth.dto.RegisterRequest;
import com.ptit.tour.auth.dto.UserInfoResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse register(RegisterRequest request);
    UserInfoResponse me(Long userId);
}
