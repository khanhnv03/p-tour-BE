package com.ptit.tour.auth.dto;

public record AuthResponse(
    String accessToken,
    String tokenType,
    Long userId,
    String email,
    String fullName,
    String role
) {
    public static AuthResponse of(String token, Long id, String email, String name, String role) {
        return new AuthResponse(token, "Bearer", id, email, name, role);
    }
}
