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
 *
 * @author WeAutoTools Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final CustomUserDetailsService userDetailsService;
    private final AuthenticationCenterService authService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // 使用认证中心服务验证并提取用户信息
            AuthenticatedUser user = authService.validateAndExtractUser(request);
            if (user != null) {
                request.setAttribute(AuthConstants.USER_INFO, user);
                request.setAttribute(AuthConstants.USER_ID, user.getUserId());
                request.setAttribute(AuthConstants.CLIENT_IP, user.getClientIp());
            }

            // 认证用户设置Spring Security Context
            if (user != null && user.isAuthenticated()) {
                setSecurityContext(request, user);
            }

        } catch (Exception ex) {
            log.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }
    
    /**
     * 设置Spring Security上下文
     */
    private void setSecurityContext(HttpServletRequest request, AuthenticatedUser authenticatedUser) {
        try {
            String username = authenticatedUser.getUsername();
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("Set security context for user: {}", username);
        } catch (Exception e) {
            log.error("Error setting security context", e);
        }
    }
}
