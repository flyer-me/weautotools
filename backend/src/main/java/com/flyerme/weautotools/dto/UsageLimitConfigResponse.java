package com.flyerme.weautotools.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 工具使用限制配置响应DTO
 *
 * @author WeAutoTools Team
 * @version 1.0.2
 * @since 2025-09-12
 */
@Data
public class UsageLimitConfigResponse {

    /**
     * 配置ID
     */
    private Long id;

    /**
     * 工具类型
     */
    private String toolType;

    /**
     * 工具名称
     */
    private String toolName;

    /**
     * 用户类型 (ANONYMOUS/LOGIN)
     */
    private String userType;

    /**
     * 限制类型 (DAILY/HOURLY/TOTAL)
     */
    private String limitType;

    /**
     * 限制次数
     */
    private Integer limitCount;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}