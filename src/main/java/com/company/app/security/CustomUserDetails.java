package com.company.app.security;

import com.company.app.admin.AdminUser;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final Long id;
    private final String email;
    private final String password;
    private final String fullName;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean active;

    public static CustomUserDetails create(AdminUser adminUser) {
        return new CustomUserDetails(
                adminUser.getId(),
                adminUser.getEmail(),
                adminUser.getPasswordHash(),
                adminUser.getFullName(),
                Collections.singletonList(new SimpleGrantedAuthority(adminUser.getRole().name())),
                Boolean.TRUE.equals(adminUser.getIsActive())
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return active;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
