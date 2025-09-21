package com.flyerme.weautotools.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * OAuth注册请求DTO
 * 支持第三方OAuth提供商注册
 *
 * @author WeAutoTools Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public record OAuthRegisterRequest(
    @NotBlank(message = "OAuth提供商不能为空")
    String provider,
    
    @NotBlank(message = "授权码不能为空")
    String code,
    
    String state,
    
    String nickname
) {}