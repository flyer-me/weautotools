package com.flyerme.weautotools.controller;

import com.flyerme.weautotools.auth.IdentifierResolver;
import com.flyerme.weautotools.dto.AuthInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 基础控制器类 - 涉及用户信息获取请继承此类
 * 使用Spring Security标准组件获取用户信息
 *
 * @author WeAutoTools Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@Slf4j
public class BaseController {

    @Autowired
    private IdentifierResolver resolver;

    public AuthInfo getCurrentUserIdentifier(){
        return resolver.getCurrentUserIdentifier();
    }

    public static List<String> getCurrentUserRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return List.of();
        }

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_")) // 只取角色
                .collect(Collectors.toList());
    }
    /**
     * 判断用户是否有某个角色
     */
    public static boolean hasRole(String role) {
        if (!role.startsWith("ROLE_")) {
            role = "ROLE_" + role;
        }
        return getCurrentUserRoles().contains(role);
    }

}