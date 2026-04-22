package com.ptit.tour.domain.user.service;

import com.ptit.tour.domain.user.dto.UpdateProfileRequest;
import com.ptit.tour.domain.user.dto.UserDto;
import com.ptit.tour.domain.user.entity.User;
import com.ptit.tour.domain.user.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {
    UserDto getById(Long id);
    UserDto updateProfile(Long id, UpdateProfileRequest request);
    // Admin operations
    Page<UserDto> findAll(String keyword, Pageable pageable);
    UserDto updateStatus(Long id, UserStatus status);
    User getEntityById(Long id);
}
