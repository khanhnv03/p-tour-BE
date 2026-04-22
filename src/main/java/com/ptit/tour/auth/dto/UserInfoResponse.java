package com.ptit.tour.auth.dto;

import com.ptit.tour.domain.user.entity.User;

public record UserInfoResponse(
    Long id,
    String email,
    String fullName,
    String role,
    String avatarUrl
) {
    public static UserInfoResponse from(User user) {
        return new UserInfoResponse(
            user.getId(),
            user.getEmail(),
            user.getFullName(),
            user.getRole().name(),
            user.getAvatarUrl()
        );
    }
}
