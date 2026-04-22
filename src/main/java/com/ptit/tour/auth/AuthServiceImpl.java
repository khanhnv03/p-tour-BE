package com.ptit.tour.auth;

import com.ptit.tour.auth.dto.AuthResponse;
import com.ptit.tour.auth.dto.LoginRequest;
import com.ptit.tour.auth.dto.RegisterRequest;
import com.ptit.tour.auth.dto.UserInfoResponse;
import com.ptit.tour.common.exception.BusinessException;
import com.ptit.tour.common.exception.ResourceNotFoundException;
import com.ptit.tour.common.security.JwtTokenProvider;
import com.ptit.tour.common.security.UserPrincipal;
import com.ptit.tour.domain.user.entity.User;
import com.ptit.tour.domain.user.enums.UserRole;
import com.ptit.tour.domain.user.enums.UserStatus;
import com.ptit.tour.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        String token = tokenProvider.generateToken(principal);
        return AuthResponse.of(token, principal.getId(), principal.getEmail(),
            principal.getUsername(), principal.getRole());
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email đã được sử dụng", HttpStatus.CONFLICT);
        }
        User user = User.builder()
            .email(request.email())
            .passwordHash(passwordEncoder.encode(request.password()))
            .fullName(request.fullName())
            .phone(request.phone())
            .role(UserRole.CUSTOMER)
            .status(UserStatus.ACTIVE)
            .build();
        userRepository.save(user);

        UserPrincipal principal = UserPrincipal.of(user);
        String token = tokenProvider.generateToken(principal);
        return AuthResponse.of(token, user.getId(), user.getEmail(), user.getFullName(),
            user.getRole().name());
    }

    @Override
    public UserInfoResponse me(Long userId) {
        return userRepository.findById(userId)
            .map(UserInfoResponse::from)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }
}
