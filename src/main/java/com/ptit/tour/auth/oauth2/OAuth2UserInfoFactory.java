package com.ptit.tour.auth.oauth2;

import com.ptit.tour.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

import java.util.Map;

public class OAuth2UserInfoFactory {

    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> new GoogleOAuth2UserInfo(attributes);
            case "facebook" -> new FacebookOAuth2UserInfo(attributes);
            default -> throw new BusinessException("OAuth2 provider không được hỗ trợ: " + registrationId, HttpStatus.BAD_REQUEST);
        };
    }
}
