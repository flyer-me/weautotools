package com.flyerme.weautotools.auth;

import com.flyerme.weautotools.dao.RoleMapper;
import com.flyerme.weautotools.dao.UserMapper;
import com.flyerme.weautotools.entity.Role;
import com.flyerme.weautotools.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;

    public CustomUserDetailsService(UserMapper userMapper, RoleMapper roleMapper) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
    }

    @Override
    public CustomUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user;
        if (username.contains("@")) {
            // 邮箱登录
            user = userMapper.findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));

        } else if (username.matches("^\\+?[1-9]\\d{1,14}$")) {
            // 手机号登录
            user = userMapper.findByPhone(username)
                    .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));
        } else {
            throw new UsernameNotFoundException("用户不存在: 该用户名无已知的处理方式: " + username);
        }

        return new CustomUserDetails(
                user.getId(),
                user.getUsername(),
                user.getPasswordHash(),
                loadAuthorities(user),
                user.getEnabled(),
                user.getLockedUntil() == null ||
                        user.getLockedUntil().isBefore(Instant.now()));
    }

    private Collection<? extends GrantedAuthority> loadAuthorities(User user) {
        var roles = roleMapper.getRolesByUserId(user.getId());
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .toList();
        /*
        return user.getRoles().stream()
                .map(role -> (GrantedAuthority) role::getName)
                .collect(Collectors.toList());
         */
    }
}
