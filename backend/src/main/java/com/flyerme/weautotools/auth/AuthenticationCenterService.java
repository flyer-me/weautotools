package com.flyerme.weautotools.auth;

import com.flyerme.weautotools.dto.AuthenticatedUser;
import com.flyerme.weautotools.dto.TokenResponse;
import com.flyerme.weautotools.entity.User;
import com.flyerme.weautotools.util.IpUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 认证中心服务 - 简化版
 * 只保留核心认证功能，移除过度设计的方法
 *
 * @author WeAutoTools Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationCenterService {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtTokenBlacklist jwtTokenBlacklist;

    /**
     * 验证请求并提取用户信息
     */
    public AuthenticatedUser validateAndExtractUser(HttpServletRequest request) {
        try {
            String token = JwtTokenProvider.extractTokenFromRequest(request);
            if (token == null || jwtTokenBlacklist.isBlacklisted(token) || !jwtTokenProvider.validateToken(token)) {
                return createAnonymousUser(request);
            }

            return extractUserFromToken(token, request);
        } catch (Exception e) {
            log.debug("Token validation failed: {}", e.getMessage());
            return createAnonymousUser(request);
        }
    }

    /**
     * 生成Token响应
     */
    public TokenResponse generateTokenResponse(Authentication authentication) {
        String token = jwtTokenProvider.generateToken(authentication);
        User user = (User) authentication.getPrincipal();
        AuthenticatedUser authenticatedUser = convertToAuthenticatedUser(user);
        return TokenResponse.success(token, 900L, authenticatedUser); // 15分钟
    }

    /**
     * 使Token失效
     */
    public void invalidateToken(String token) {
        if (token != null && jwtTokenProvider.validateToken(token)) {
            Date expiration = jwtTokenProvider.getExpirationDateFromToken(token);
            if (expiration != null) {
                jwtTokenBlacklist.add(token, expiration);
            }
        }
    }

    /**
     * 获取当前用户信息
     */
    public AuthenticatedUser getCurrentUser(HttpServletRequest request) {
        Object userAttr = request.getAttribute(AuthConstants.USER_INFO);
        if (userAttr instanceof AuthenticatedUser) {
            return (AuthenticatedUser) userAttr;
        }
        return validateAndExtractUser(request);
    }

    /**
     * 将用户信息设置到请求属性
     */
    public void setUserToRequest(HttpServletRequest request, AuthenticatedUser user) {
        if (user != null) {
            request.setAttribute(AuthConstants.USER_INFO, user);
            request.setAttribute(AuthConstants.USER_ID, user.getUserId());
            request.setAttribute(AuthConstants.CLIENT_IP, user.getClientIp());
        }
    }

    // 私有辅助方法
    private AuthenticatedUser extractUserFromToken(String token, HttpServletRequest request) {
        String username = jwtTokenProvider.getUsernameFromJWT(token);
        Long userId = jwtTokenProvider.getUserIdFromJWT(token);
        
        if (username == null || userId == null) {
            return null;
        }

        return AuthenticatedUser.builder()
                .userId(userId)
                .username(username)
                .userType(AuthConstants.ROLE_USER)
                .authorities(List.of(AuthConstants.ROLE_USER))
                .lastLoginTime(Instant.now())
                .clientIp(request != null ? IpUtils.getClientIpAddress(request) : null)
                .build();
    }

    private AuthenticatedUser createAnonymousUser(HttpServletRequest request) {
        return AuthenticatedUser.builder()
                .userType(AuthConstants.ROLE_ANONYMOUS)
                .authorities(List.of(AuthConstants.ROLE_ANONYMOUS))
                .clientIp(request != null ? IpUtils.getClientIpAddress(request) : null)
                .build();
    }

    private AuthenticatedUser convertToAuthenticatedUser(User user) {
        List<String> authorities = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return AuthenticatedUser.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .userType(authorities.contains(AuthConstants.ROLE_ADMIN) ? AuthConstants.ROLE_ADMIN : AuthConstants.ROLE_USER)
                .authorities(authorities)
                .lastLoginTime(Instant.now())
                .build();
    }
}