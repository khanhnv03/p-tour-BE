package com.ptit.tour.domain.user.dto;

import com.ptit.tour.domain.user.entity.User;
import com.ptit.tour.domain.user.enums.UserRole;
import com.ptit.tour.domain.user.enums.UserStatus;

import java.time.Instant;

public record UserDto(
    Long id,
    String email,
    String fullName,
    String phone,
    String avatarUrl,
    String address,
    UserRole role,
    UserStatus status,
    Instant emailVerifiedAt,
    Instant createdAt
) {
    public static UserDto from(User user) {
        return new UserDto(
            user.getId(), user.getEmail(), user.getFullName(),
            user.getPhone(), user.getAvatarUrl(), user.getAddress(),
            user.getRole(), user.getStatus(),
            user.getEmailVerifiedAt(), user.getCreatedAt()
        );
    }
}
