package com.flyerme.weautotools.entity;

import com.flyerme.weautotools.common.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.net.InetAddress;

/**
 * 工具使用记录实体类
 *
 * @author WeAutoTools Team
 * @version 1.0.2
 * @since 2025-09-12
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ToolUsageRecord extends BaseEntity {

    /**
     * 用户ID (登录用户，匿名用户为NULL)
     */
    private Long userId;

    /**
     * 用户标识 (IP哈希或设备指纹)
     */
    @NotBlank(message = "用户标识不能为空")
    private String userIdentifier;

    /**
     * 工具类型
     */
    @NotBlank(message = "工具类型不能为空")
    private String toolType;

    /**
     * 工具名称
     */
    @NotBlank(message = "工具名称不能为空")
    private String toolName;

    /**
     * 使用时间
     */
    private LocalDateTime usageTime;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 用户代理
     */
    private String userAgent;
}