package com.ptit.tour.domain.user.service;

import com.ptit.tour.domain.user.dto.ChangePasswordRequest;
import com.ptit.tour.domain.user.dto.AdminCustomerDetailDto;
import com.ptit.tour.domain.user.dto.NotificationPreferencesDto;
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
    void changePassword(Long id, ChangePasswordRequest request);
    NotificationPreferencesDto getNotificationPreferences(Long userId);
    NotificationPreferencesDto updateNotificationPreferences(Long userId, NotificationPreferencesDto dto);
    // Admin operations
    Page<UserDto> findAll(String keyword, Pageable pageable);
    AdminCustomerDetailDto getCustomerDetail(Long id);
    UserDto updateAdmin(Long id, UpdateProfileRequest request);
    UserDto updateStatus(Long id, UserStatus status);
    User getEntityById(Long id);
}
