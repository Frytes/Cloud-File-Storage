package com.frytes.cloudstorage.users.security;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

@Getter
@EqualsAndHashCode(callSuper = true)
public class CustomUserDetails extends User implements OAuth2User {

    private final Long id;
    private transient Map<String, Object> attributes;

    public CustomUserDetails(Long id, String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.id = id;
    }

    public CustomUserDetails(Long id, String username, String password, Collection<? extends GrantedAuthority> authorities, Map<String, Object> attributes) {
        super(username, password, authorities);
        this.id = id;
        this.attributes = attributes;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return this.getUsername();
    }
}