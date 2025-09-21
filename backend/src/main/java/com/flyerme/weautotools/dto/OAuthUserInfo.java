package com.flyerme.weautotools.dto;

/**
 * OAuth用户信息DTO
 * 从OAuth提供商获取的用户信息
 *
 * @author WeAutoTools Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public record OAuthUserInfo(
    String id,
    String name,
    String email,
    String mobile,
    String avatarUrl,
    String provider
) {}