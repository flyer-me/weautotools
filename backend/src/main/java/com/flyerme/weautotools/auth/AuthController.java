package com.flyerme.weautotools.auth;

import com.flyerme.weautotools.common.Result;
import com.flyerme.weautotools.dao.UserMapper;
import com.flyerme.weautotools.dto.LoginRequest;
import com.flyerme.weautotools.dto.RegisterRequest;
import com.flyerme.weautotools.entity.User;
import com.flyerme.weautotools.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserMapper userMapper;
    private final UserService userService;
    private final OAuth2AuthorizationService authorizationService;
    private final AuthenticationManager authenticationManager;

    /**
     * 用户注册
     * 注册成功后返回成功响应
     */
    @PostMapping("/register")
    public Result<String> register(@RequestBody @Valid RegisterRequest request) {
        log.info("用户注册请求(0-4): {}", request.getIdentifier().substring(0, 4));
        if (isNotValidRegisterRequest(request)) {
            return Result.error("请提供手机号码或邮箱等注册方式");
        }
        
        log.info("用户注册请求: 类型={}", request.registerType());

        try {
            // 检查用户是否已存在
            String identifier = request.getIdentifier();
            Optional<User> existingUser = findExistingUser(identifier);
            if (existingUser.isPresent()) {
                return Result.error("用户已存在");
            }

            // 在一个事务中创建新用户并分配默认角色
            userService.createUserWithDefaultRole(request);
            
            log.info("用户注册成功: identifier={}", identifier);
            return Result.success("注册成功，请使用OAuth2端点获取令牌");
            
        } catch (Exception e) {
            log.error("用户注册失败: {}", request.getIdentifier(), e);
            return Result.error("注册失败: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public Result<String> login(@RequestBody @Valid LoginRequest request, HttpServletRequest servletRequest) {

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(request.Identifier(), request.password());
        try{
            Authentication auth = authenticationManager.authenticate(authToken);
            if (auth.isAuthenticated()) {
                SecurityContextHolder.getContext().setAuthentication(auth);
                servletRequest.getSession(true).setAttribute(
                        HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                        SecurityContextHolder.getContext()
                );
                return Result.success("登录成功");
            }
        }
        catch (AuthenticationException e)
        {
            log.error("用户登录失败: {}", request.Identifier().substring(0, 4), e);
        }
        return Result.error("登录失败");
    }
    
    /**
     * 用户登出
     * 撤销访问令牌和刷新令牌
     */
    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest request) {
        try {
            String token = extractTokenFromRequest(request);
            if (token != null) {
                // 撤销令牌
                revokeToken(token);
                log.info("用户登出成功，令牌已撤销");
                return Result.success("登出成功，令牌已撤销");
            } else {
                log.info("用户登出请求，但未找到令牌");
                return Result.success("登出成功");
            }
        } catch (Exception e) {
            log.error("登出过程中发生错误", e);
            return Result.success("登出完成");
        }
    }
    
    @GetMapping("/authorize-url")
    public Result<String> getAuthorizeUrl() {
        String authorizeUrl = "http://localhost:8080/oauth2/authorize?" +
                "response_type=code&" +
                "client_id=weautotools-client&" +
                "redirect_uri=http://localhost:8080/callback&" +
                "scope=read write openid profile&" +
                "state=random_state";
        
        return Result.success("授权URL", authorizeUrl);
    }

    // ========== 私有辅助方法 ==========

    private boolean isNotValidRegisterRequest(@Valid RegisterRequest request) {
        return request.mobile() == null && request.email() == null;
    }

    private Optional<User> findExistingUser(String identifier) {
        if (identifier.contains("@")) {
            return userMapper.findByEmail(identifier);
        } else {
            return userMapper.findByPhone(identifier);
        }
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

        private void revokeToken(String token) {
        try {
            // 首先尝试作为访问令牌查找
            OAuth2Authorization authorization = authorizationService.findByToken(
                token, OAuth2TokenType.ACCESS_TOKEN);
            
            // 如果没找到，尝试作为刷新令牌查找
            if (authorization == null) {
                authorization = authorizationService.findByToken(
                    token, OAuth2TokenType.REFRESH_TOKEN);
            }
            
            if (authorization != null) {
                // 创建新的授权对象，将令牌标记为已撤销
                OAuth2Authorization.Builder builder = OAuth2Authorization.from(authorization);
                
                // 撤销访问令牌
                OAuth2Authorization.Token<OAuth2AccessToken> accessToken = 
                    authorization.getAccessToken();
                if (accessToken != null && !accessToken.isInvalidated()) {
                    builder.token(accessToken.getToken(), metadata -> 
                        metadata.put(OAuth2Authorization.Token.INVALIDATED_METADATA_NAME, true));
                    log.debug("访问令牌已撤销");
                }
                
                // 撤销刷新令牌
                OAuth2Authorization.Token<OAuth2RefreshToken> refreshToken = 
                    authorization.getRefreshToken();
                if (refreshToken != null && !refreshToken.isInvalidated()) {
                    builder.token(refreshToken.getToken(), metadata -> 
                        metadata.put(OAuth2Authorization.Token.INVALIDATED_METADATA_NAME, true));
                    log.debug("刷新令牌已撤销");
                }
                
                // 保存撤销后的授权信息
                OAuth2Authorization revokedAuthorization = builder.build();
                authorizationService.save(revokedAuthorization);
                log.info("OAuth2授权已撤销");
            } else {
                log.warn("未找到对应的OAuth2授权，令牌可能已过期或无效");
            }
        } catch (Exception e) {
            log.error("撤销令牌时发生错误", e);
            throw new RuntimeException("撤销令牌失败", e);
        }
    }

}