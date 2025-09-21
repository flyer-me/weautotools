package com.flyerme.weautotools.dto;

/**
 * 认证信息 authenticated = true时，字段均有效，否则userId为null
 */
public record AuthInfo(
        boolean authenticated,
        Long userId,
        String userIdentifier
) {
}