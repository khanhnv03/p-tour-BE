package com.ptit.tour.auth.oauth2;

import java.util.Map;

public class FacebookOAuth2UserInfo extends OAuth2UserInfo {

    public FacebookOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override public String getId() { return (String) attributes.get("id"); }
    @Override public String getEmail() { return (String) attributes.get("email"); }
    @Override public String getName() { return (String) attributes.get("name"); }

    @SuppressWarnings("unchecked")
    @Override
    public String getAvatarUrl() {
        Map<String, Object> picture = (Map<String, Object>) attributes.get("picture");
        if (picture == null) return null;
        Map<String, Object> data = (Map<String, Object>) picture.get("data");
        if (data == null) return null;
        return (String) data.get("url");
    }
}
