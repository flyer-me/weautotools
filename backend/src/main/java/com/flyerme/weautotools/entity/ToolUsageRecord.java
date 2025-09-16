package com.flyerme.weautotools.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.flyerme.weautotools.common.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 工具使用记录实体类
 *
 * @author WeAutoTools Team
 * @version 1.0.3
 * @since 2025-09-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tool_usage_records")
public class ToolUsageRecord extends BaseEntity {

    /**
     * 用户ID (登录用户，匿名用户为NULL)
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 用户标识 (IP哈希或设备指纹)
     */
    @NotBlank(message = "用户标识不能为空")
    @TableField("user_identifier")
    private String userIdentifier;

    /**
     * 关联的工具ID
     */
    @NotNull(message = "工具ID不能为空")
    @TableField("tool_id")
    private Long toolId;

    /**
     * 工具类型（保留用于兼容性，建议使用toolId）
     */
    @TableField("tool_type")
    private String toolType;

    /**
     * 工具名称（保留用于兼容性，建议使用toolId）
     */
    @TableField("tool_name")
    private String toolName;

    /**
     * 使用时间
     */
    @TableField("usage_time")
    private LocalDateTime usageTime;

    /**
     * IP地址
     */
    @TableField("ip_address")
    private String ipAddress;

    /**
     * 用户代理
     */
    @TableField("user_agent")
    private String userAgent;
}