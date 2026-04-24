package com.ptit.tour.auth;

import com.ptit.tour.auth.dto.AuthResponse;
import com.ptit.tour.auth.dto.ForgotPasswordRequest;
import com.ptit.tour.auth.dto.LoginRequest;
import com.ptit.tour.auth.dto.RegisterRequest;
import com.ptit.tour.auth.dto.ResetPasswordRequest;
import com.ptit.tour.auth.dto.UserInfoResponse;
import com.ptit.tour.auth.entity.PasswordResetToken;
import com.ptit.tour.auth.repository.PasswordResetTokenRepository;
import com.ptit.tour.common.exception.BusinessException;
import com.ptit.tour.common.exception.ResourceNotFoundException;
import com.ptit.tour.common.mail.EmailService;
import com.ptit.tour.common.security.JwtTokenProvider;
import com.ptit.tour.common.security.UserPrincipal;
import com.ptit.tour.domain.user.entity.User;
import com.ptit.tour.domain.user.enums.UserRole;
import com.ptit.tour.domain.user.enums.UserStatus;
import com.ptit.tour.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

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

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        // Return silently if email not found to prevent user enumeration
        userRepository.findByEmail(request.email()).ifPresent(user -> {
            passwordResetTokenRepository.deleteByUserId(user.getId());
            PasswordResetToken prt = PasswordResetToken.builder()
                    .user(user)
                    .token(UUID.randomUUID().toString())
                    .expiresAt(Instant.now().plus(30, ChronoUnit.MINUTES))
                    .used(false)
                    .build();
            passwordResetTokenRepository.save(prt);
            String resetLink = frontendUrl + "/reset-password?token=" + prt.getToken();
            emailService.sendPasswordResetEmail(user.getEmail(), user.getFullName(), resetLink, prt.getExpiresAt());
            log.info("Password reset email queued for {}", user.getEmail());
        });
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken prt = passwordResetTokenRepository.findByToken(request.token())
                .orElseThrow(() -> new BusinessException("Token không hợp lệ", HttpStatus.BAD_REQUEST));
        if (prt.isExpiredOrUsed()) {
            throw new BusinessException("Token đã hết hạn hoặc đã được sử dụng", HttpStatus.BAD_REQUEST);
        }
        User user = prt.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        prt.setUsed(true);
        passwordResetTokenRepository.save(prt);
    }
}
