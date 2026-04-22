package com.ptit.tour.domain.user.service;

import com.ptit.tour.common.exception.ResourceNotFoundException;
import com.ptit.tour.common.security.UserPrincipal;
import com.ptit.tour.domain.user.dto.UpdateProfileRequest;
import com.ptit.tour.domain.user.dto.UserDto;
import com.ptit.tour.domain.user.entity.User;
import com.ptit.tour.domain.user.enums.UserStatus;
import com.ptit.tour.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

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
        if (keyword != null && !keyword.isBlank()) {
            return userRepository.findByFullNameContainingIgnoreCase(keyword, pageable)
                .map(UserDto::from);
        }
        return userRepository.findAll(pageable).map(UserDto::from);
    }

    @Override
    @Transactional
    public UserDto updateStatus(Long id, UserStatus status) {
        User user = getEntityById(id);
        user.setStatus(status);
        return UserDto.from(userRepository.save(user));
    }

    @Override
    public User getEntityById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }
}
