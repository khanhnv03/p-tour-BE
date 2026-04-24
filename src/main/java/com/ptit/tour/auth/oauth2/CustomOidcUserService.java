package com.ptit.tour.auth.oauth2;

import com.ptit.tour.common.security.UserPrincipal;
import com.ptit.tour.domain.user.entity.User;
import com.ptit.tour.domain.user.enums.UserRole;
import com.ptit.tour.domain.user.enums.UserStatus;
import com.ptit.tour.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class CustomOidcUserService extends OidcUserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(
            registrationId, oidcUser.getAttributes()
        );

        if (userInfo.getEmail() == null || userInfo.getEmail().isBlank()) {
            throw new OAuth2AuthenticationException(
                new OAuth2Error("email_not_found"), "Email không tìm thấy từ OAuth2 provider"
            );
        }

        User user = userRepository.findByEmail(userInfo.getEmail())
            .map(existing -> updateExistingUser(existing, userInfo))
            .orElseGet(() -> createNewUser(registrationId, userInfo));

        return UserPrincipal.ofOidc(user, oidcUser);
    }

    private User createNewUser(String provider, OAuth2UserInfo userInfo) {
        User user = User.builder()
            .email(userInfo.getEmail())
            .fullName(userInfo.getName() != null ? userInfo.getName() : userInfo.getEmail())
            .avatarUrl(userInfo.getAvatarUrl())
            .provider(provider)
            .providerId(userInfo.getId())
            .role(UserRole.CUSTOMER)
            .status(UserStatus.ACTIVE)
            .emailVerifiedAt(Instant.now())
            .build();
        return userRepository.save(user);
    }

    private User updateExistingUser(User user, OAuth2UserInfo userInfo) {
        if (user.getAvatarUrl() == null && userInfo.getAvatarUrl() != null) {
            user.setAvatarUrl(userInfo.getAvatarUrl());
            return userRepository.save(user);
        }
        return user;
    }
}
