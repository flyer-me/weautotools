package com.flyerme.weautotools.auth;

import com.flyerme.weautotools.dto.AuthenticatedUser;
import com.flyerme.weautotools.dto.TokenResponse;
import com.flyerme.weautotools.entity.User;
import com.flyerme.weautotools.util.IpUtils;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.List;

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
    
    @Value("${app.jwt.expiration-in-ms}")
    private long jwtExpirationInMs;

    /**
     * 验证请求并提取用户信息
     */
    @Nullable
    public AuthenticatedUser validateAndExtractUser(HttpServletRequest request) {
        try {
            String token = JwtTokenProvider.extractTokenFromRequest(request);
            if (token == null || jwtTokenBlacklist.isBlacklisted(token) || !jwtTokenProvider.validateToken(token)) {
                // return createAnonymousUser(request);
                return null;
            }

            return extractUserFromToken(token, request);
        } catch (Exception e) {
            log.debug("Token validation failed: {}", e.getMessage());
            // return createAnonymousUser(request);
            return null;
        }
    }

    /**
     * 生成Token响应
     */
    public TokenResponse generateTokenResponse(Authentication authentication) {
        String token = jwtTokenProvider.generateToken(authentication);
        User user = (User) authentication.getPrincipal();
        AuthenticatedUser authenticatedUser = convertToAuthenticatedUser(user);
        
        // 计算过期时间（秒）- 与JWT实际过期时间保持一致
        long expiresInSeconds = jwtExpirationInMs / 1000;
        
        return TokenResponse.success(token, expiresInSeconds, authenticatedUser);
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
        List<String> authorities = List.of(user.getRole() != null ? user.getRole() : AuthConstants.ROLE_USER);

        return AuthenticatedUser.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .userType(authorities.contains(AuthConstants.ROLE_ADMIN) ? AuthConstants.ROLE_ADMIN : AuthConstants.ROLE_USER)
                .authorities(authorities)
                .lastLoginTime(Instant.now())
                .build();
    }
}