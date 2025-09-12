package com.flyerme.weautotools.service;

import com.flyerme.weautotools.entity.ToolUsageLimit;

/**
 * 使用限制服务接口
 *
 * @author WeAutoTools Team
 * @version 1.0.2
 * @since 2025-09-12
 */
public interface UsageLimitService {

    /**
     * 检查是否超出使用限制
     *
     * @param userIdentifier 用户标识
     * @param toolName      工具名称
     * @param userType      用户类型
     * @return 是否超出限制
     */
    boolean isExceededLimit(String userIdentifier, String toolName, ToolUsageLimit.UserType userType);

    /**
     * 记录工具使用
     *
     * @param userIdentifier 用户标识
     * @param toolName      工具名称
     * @param userType      用户类型
     * @param userId        用户ID (可选)
     * @param ipAddress     IP地址
     * @param userAgent     用户代理
     */
    void recordUsage(String userIdentifier, String toolName, ToolUsageLimit.UserType userType, 
                    Long userId, String ipAddress, String userAgent);

    /**
     * 获取剩余使用次数
     *
     * @param userIdentifier 用户标识
     * @param toolName      工具名称
     * @param userType      用户类型
     * @return 剩余使用次数
     */
    int getRemainingUsage(String userIdentifier, String toolName, ToolUsageLimit.UserType userType);

    /**
     * 重置用户使用次数
     *
     * @param userIdentifier 用户标识
     */
    void resetUserUsage(String userIdentifier);

    /**
     * 获取工具的使用限制配置
     *
     * @param toolName 工具名称
     * @param userType 用户类型
     * @return 使用限制配置
     */
    ToolUsageLimit getUsageLimit(String toolName, ToolUsageLimit.UserType userType);
}