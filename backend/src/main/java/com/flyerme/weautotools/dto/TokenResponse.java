package com.flyerme.weautotools.dto;

/**
 * Token响应数据传输对象
 * 标准化JWT令牌返回格式（简化版本）
 *
 * @author WeAutoTools Team
 * @version 1.0.1
 * @since 2025-01-01
 */
public record TokenResponse(
    String accessToken,
    Long expiresIn,
    String refreshToken,
    String tokenType)
{

    /**
     * 创建成功的Token响应
     */
    public static TokenResponse success(String accessToken,
                                        Long expiresIn) {
        return new TokenResponse(accessToken, expiresIn,
                "Bearer", null);
    }
    
    /**
     * 创建包含刷新令牌的Token响应
     */
    public static TokenResponse withRefresh(String accessToken,
                                            Long expiresIn,
                                            String refreshToken) {
        return new TokenResponse(accessToken, expiresIn, refreshToken,
                "Bearer");
    }

}