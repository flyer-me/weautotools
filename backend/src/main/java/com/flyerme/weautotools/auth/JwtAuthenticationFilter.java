package com.flyerme.weautotools.auth;

import com.flyerme.weautotools.dto.AuthenticatedUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT认证过滤器
 * 集成AuthenticationCenterService，增强用户信息设置到RequestAttribute
 *
 * @author WeAutoTools Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;
    private final JwtTokenBlacklist jwtTokenBlacklist;
    private final AuthenticationCenterService authenticationCenterService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // 使用认证中心服务验证并提取用户信息
            AuthenticatedUser authenticatedUser = authenticationCenterService.validateAndExtractUser(request);
            
            // 设置用户信息到RequestAttribute
            authenticationCenterService.setUserToRequest(request, authenticatedUser);
            
            // 如果用户已认证，设置Spring Security上下文
            if (authenticatedUser != null && authenticatedUser.isAuthenticated()) {
                setSecurityContext(request, authenticatedUser);
            }
            
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }
    
    /**
     * 设置Spring Security上下文
     * 保持与现有认证机制的兼容性
     */
    private void setSecurityContext(HttpServletRequest request, AuthenticatedUser authenticatedUser) {
        try {
            String jwt = getJwtFromRequest(request);
            if (StringUtils.hasText(jwt) && !jwtTokenBlacklist.isBlacklisted(jwt) && tokenProvider.validateToken(jwt)) {
                String username = authenticatedUser.getUsername();
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Set security context for user: {}", username);
            }
        } catch (Exception e) {
            log.error("Error setting security context", e);
        }
    }

    /**
     * 从请求中提取JWT令牌
     * 复用JwtTokenProvider的实现
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        return JwtTokenProvider.extractTokenFromRequest(request);
    }
}
