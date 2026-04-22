package com.ptit.tour.common.security;

import com.ptit.tour.domain.user.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Getter
public class UserPrincipal implements UserDetails, OAuth2User {

    private final Long id;
    private final String email;
    private final String password;
    private final String role;
    private final boolean enabled;
    private final Collection<? extends GrantedAuthority> authorities;
    private Map<String, Object> attributes;

    private UserPrincipal(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.password = user.getPasswordHash();
        this.role = user.getRole().name();
        this.enabled = user.getStatus().name().equals("ACTIVE");
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    public static UserPrincipal of(User user) {
        return new UserPrincipal(user);
    }

    public static UserPrincipal ofOAuth2(User user, Map<String, Object> attributes) {
        UserPrincipal principal = new UserPrincipal(user);
        principal.attributes = attributes;
        return principal;
    }

    @Override public String getUsername() { return email; }
    @Override public String getName() { return email; }
    @Override public Map<String, Object> getAttributes() { return attributes != null ? attributes : Map.of(); }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return enabled; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return enabled; }
}
