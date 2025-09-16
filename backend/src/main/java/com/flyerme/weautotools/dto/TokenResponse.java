package com.flyerme.weautotools.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Token响应数据传输对象
 * 标准化JWT令牌返回格式
 *
 * @author WeAutoTools Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponse {
    
    /**
     * 访问令牌
     */
    private String accessToken;
    
    /**
     * 令牌过期时间（秒）
     */
    private Long expiresIn;
    
    /**
     * 令牌类型，通常为 "Bearer"
     */
    @Builder.Default
    private String tokenType = "Bearer";
    
    /**
     * 认证用户信息
     */
    private AuthenticatedUser user;
    
    /**
     * 刷新令牌（可选）
     */
    private String refreshToken;
    
    /**
     * 创建成功的Token响应
     */
    public static TokenResponse success(String accessToken, Long expiresIn, AuthenticatedUser user) {
        return TokenResponse.builder()
                .accessToken(accessToken)
                .expiresIn(expiresIn)
                .tokenType("Bearer")
                .user(user)
                .build();
    }
    
    /**
     * 创建包含刷新令牌的Token响应
     */
    public static TokenResponse withRefresh(String accessToken, String refreshToken, Long expiresIn, AuthenticatedUser user) {
        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(expiresIn)
                .tokenType("Bearer")
                .user(user)
                .build();
    }
}