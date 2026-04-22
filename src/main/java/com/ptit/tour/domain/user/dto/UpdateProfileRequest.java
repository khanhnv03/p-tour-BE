package com.ptit.tour.domain.user.dto;

import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
    @Size(max = 255) String fullName,
    @Size(max = 20) String phone,
    @Size(max = 500) String avatarUrl,
    String address
) {}
