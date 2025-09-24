package com.flyerme.weautotools.auth;

import com.flyerme.weautotools.dao.RoleMapper;
import com.flyerme.weautotools.dao.UserMapper;
import com.flyerme.weautotools.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collection;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;

    public CustomUserDetailsService(UserMapper userMapper, RoleMapper roleMapper) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
    }

    /**
     * 登录时，根据用户标识获得UserDetails
     *
     * @param username 用户登录标识
     * @return 用户信息
     * @throws UsernameNotFoundException 用户不存在
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
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

        return new org.springframework.security.core.userdetails.User(
                String.valueOf(user.getId()),
                user.getPasswordHash(),
                loadAuthorities(user));
    }

    private Collection<? extends GrantedAuthority> loadAuthorities(User user) {
        var roles = roleMapper.getRolesByUserId(user.getId());
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .toList();
    }
}
