package com.ptit.tour.domain.user.service;

import com.ptit.tour.common.exception.BusinessException;
import com.ptit.tour.common.exception.ResourceNotFoundException;
import com.ptit.tour.common.security.UserPrincipal;
import com.ptit.tour.domain.booking.dto.BookingDto;
import com.ptit.tour.domain.booking.repository.BookingRepository;
import com.ptit.tour.domain.order.repository.OrderRepository;
import com.ptit.tour.domain.user.dto.AdminCustomerDetailDto;
import com.ptit.tour.domain.user.dto.ChangePasswordRequest;
import com.ptit.tour.domain.user.dto.NotificationPreferencesDto;
import com.ptit.tour.domain.user.dto.UpdateProfileRequest;
import com.ptit.tour.domain.user.dto.UserDto;
import com.ptit.tour.domain.user.entity.User;
import com.ptit.tour.domain.user.entity.UserNotificationPreferences;
import com.ptit.tour.domain.user.enums.UserStatus;
import com.ptit.tour.domain.user.repository.UserNotificationPreferencesRepository;
import com.ptit.tour.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserNotificationPreferencesRepository notifPrefsRepository;
    private final PasswordEncoder passwordEncoder;
    private final BookingRepository bookingRepository;
    private final OrderRepository orderRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        return UserPrincipal.of(user);
    }

    @Override
    public UserDto getById(Long id) {
        return UserDto.from(getEntityById(id));
    }

    @Override
    @Transactional
    public UserDto updateProfile(Long id, UpdateProfileRequest request) {
        User user = getEntityById(id);
        if (request.fullName() != null) user.setFullName(request.fullName());
        if (request.phone() != null) user.setPhone(request.phone());
        if (request.avatarUrl() != null) user.setAvatarUrl(request.avatarUrl());
        if (request.address() != null) user.setAddress(request.address());
        return UserDto.from(userRepository.save(user));
    }

    @Override
    public Page<UserDto> findAll(String keyword, Pageable pageable) {
        return userRepository.searchAdmin(normalize(keyword), pageable).map(UserDto::from);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminCustomerDetailDto getCustomerDetail(Long id) {
        User user = getEntityById(id);
        var recentBookings = bookingRepository.findTop5ByUserIdOrderByCreatedAtDesc(id)
            .stream().map(BookingDto::from).toList();
        return new AdminCustomerDetailDto(
            UserDto.from(user),
            bookingRepository.countByUserId(id),
            orderRepository.sumPaidAmountByUserId(id),
            recentBookings.isEmpty() ? null : recentBookings.get(0).createdAt(),
            recentBookings
        );
    }

    @Override
    @Transactional
    public UserDto updateAdmin(Long id, UpdateProfileRequest request) {
        return updateProfile(id, request);
    }

    @Override
    @Transactional
    public UserDto updateStatus(Long id, UserStatus status) {
        User user = getEntityById(id);
        user.setStatus(status);
        return UserDto.from(userRepository.save(user));
    }

    @Override
    @Transactional
    public void changePassword(Long id, ChangePasswordRequest request) {
        User user = getEntityById(id);
        if (user.getPasswordHash() == null || !passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BusinessException("Mật khẩu hiện tại không đúng", HttpStatus.BAD_REQUEST);
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    @Override
    public NotificationPreferencesDto getNotificationPreferences(Long userId) {
        User user = getEntityById(userId);
        UserNotificationPreferences prefs = notifPrefsRepository.findByUserId(userId)
                .orElseGet(() -> notifPrefsRepository.save(UserNotificationPreferences.defaultFor(user)));
        return NotificationPreferencesDto.from(prefs);
    }

    @Override
    @Transactional
    public NotificationPreferencesDto updateNotificationPreferences(Long userId, NotificationPreferencesDto dto) {
        User user = getEntityById(userId);
        UserNotificationPreferences prefs = notifPrefsRepository.findByUserId(userId)
                .orElseGet(() -> UserNotificationPreferences.defaultFor(user));
        prefs.setBookingAlerts(dto.bookingAlerts());
        prefs.setEditorialComments(dto.editorialComments());
        prefs.setSystemStatus(dto.systemStatus());
        return NotificationPreferencesDto.from(notifPrefsRepository.save(prefs));
    }

    @Override
    public User getEntityById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
