package com.flyerme.weautotools.auth;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class CustomUserDetails implements UserDetails {

    @Getter
    private final Long userId;           // 业务主键
    private final String username;       // 登录名
    private final String password;
    private final Set<GrantedAuthority> authorities;
    private final boolean accountNonLocked;
    private final boolean enabled;

    // 构造函数
    public CustomUserDetails(Long userId, String username, String password,
                             Collection<? extends GrantedAuthority> authorities,
                             boolean enabled,
                             boolean accountNonLocked) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.authorities = Collections.unmodifiableSet(new LinkedHashSet<>(authorities));
        this.enabled = enabled;
        this.accountNonLocked = accountNonLocked;
    }

    // === 实现 UserDetails 接口方法 ===
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
        return username;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return accountNonLocked; }
    @Override
    public boolean isEnabled() { return enabled; }
}
