package com.flyerme.weautotools.dto;

import lombok.Data;

/**
 * 使用限制检查响应DTO
 *
 * @author WeAutoTools Team
 * @version 1.0.2
 * @since 2025-09-12
 */
@Data
public class UsageLimitCheckResponse {

    /**
     * 是否超出限制
     */
    private Boolean isExceeded;

    /**
     * 剩余使用次数
     */
    private Integer remaining;

    /**
     * 限制信息
     */
    private String message;

    /**
     * 工具名称
     */
    private String toolName;

    /**
     * 用户类型
     */
    private String userType;

    /**
     * 创建成功响应
     */
    public static UsageLimitCheckResponse success(String toolName, String userType, int remaining) {
        UsageLimitCheckResponse response = new UsageLimitCheckResponse();
        response.setIsExceeded(false);
        response.setRemaining(remaining);
        response.setMessage("使用正常");
        response.setToolName(toolName);
        response.setUserType(userType);
        return response;
    }

    /**
     * 创建限制响应
     */
    public static UsageLimitCheckResponse exceeded(String toolName, String userType, String message) {
        UsageLimitCheckResponse response = new UsageLimitCheckResponse();
        response.setIsExceeded(true);
        response.setRemaining(0);
        response.setMessage(message != null ? message : "使用次数已达限制");
        response.setToolName(toolName);
        response.setUserType(userType);
        return response;
    }
}