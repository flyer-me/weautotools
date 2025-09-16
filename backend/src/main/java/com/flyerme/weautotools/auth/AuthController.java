package com.flyerme.weautotools.auth;

import com.flyerme.weautotools.dto.TokenResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证控制器
 * 使用新的认证中心服务处理用户登录登出
 *
 * @author WeAutoTools Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final AuthenticationCenterService authenticationCenterService;

    /**
     * 用户登录
     *
     * @param loginRequest 登录请求
     * @return Token响应
     */
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> authenticateUser(@RequestBody LoginRequest loginRequest) {
        try {
            log.info("User login attempt: {}", loginRequest.username());
            
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.username(),
                            loginRequest.password()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 使用认证中心服务生成Token响应
            TokenResponse tokenResponse = authenticationCenterService.generateTokenResponse(authentication);
            
            log.info("User login successful: {}", loginRequest.username());
            return ResponseEntity.ok(tokenResponse);
            
        } catch (Exception e) {
            log.error("User login failed: {}", loginRequest.username(), e);
            throw e;
        }
    }

    /**
     * 用户登出
     *
     * @param request HTTP请求
     * @return 登出结果
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logoutUser(HttpServletRequest request) {
        try {
            String jwt = JwtTokenProvider.extractTokenFromRequest(request);
            if (jwt != null) {
                // 使用认证中心服务使Token失效
                authenticationCenterService.invalidateToken(jwt);
                log.info("User logout successful");
            }
            return ResponseEntity.ok("Successfully logged out");
        } catch (Exception e) {
            log.error("Error during logout", e);
            return ResponseEntity.ok("Logout completed");
        }
    }
}

/**
 * 登录请求记录
 */
record LoginRequest(String username, String password) {}

/**
 * JWT认证响应记录（保持向后兼容）
 */
record JwtAuthenticationResponse(String accessToken) {}

