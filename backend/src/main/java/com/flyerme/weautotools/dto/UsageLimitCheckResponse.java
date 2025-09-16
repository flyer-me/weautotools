package com.flyerme.weautotools.dto;


/**
 * 使用限制检查响应DTO
 *
 * @author WeAutoTools Team
 * @version 1.0.2
 * @since 2025-09-12
 */
public record UsageLimitCheckResponse(
        Boolean isExceeded,
        Integer remaining,
        String message,
        String toolName,
        String userType
) {
    /**
     * 创建成功响应
     */
    public static UsageLimitCheckResponse success(String toolName, String userType, int remaining) {
        return new UsageLimitCheckResponse(false, remaining, "正常", toolName, userType);
    }

    /**
     * 创建限制响应
     */
    public static UsageLimitCheckResponse exceeded(String toolName, String userType, String message) {
        return new UsageLimitCheckResponse(
                true,
                0,
                message != null ? message : "使用次数已达限制",
                toolName,
                userType);
    }
}