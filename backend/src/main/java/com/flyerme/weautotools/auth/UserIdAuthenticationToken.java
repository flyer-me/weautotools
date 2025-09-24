package com.flyerme.weautotools.auth;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * 自定义用户ID认证令牌 Object principal=用户id，避免 OAuth2Authorization 序列化对象
 */
public class UserIdAuthenticationToken extends UsernamePasswordAuthenticationToken {
    public UserIdAuthenticationToken(String userId, Object credentials,
                                     Collection<? extends GrantedAuthority> authorities) {
        super(userId, credentials, authorities);
    }

    @Override
    public String getName() {
        return (String) getPrincipal();
    }
}

