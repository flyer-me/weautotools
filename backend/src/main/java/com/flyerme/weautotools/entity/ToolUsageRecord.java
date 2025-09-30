package com.flyerme.weautotools.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.flyerme.weautotools.common.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.UUID;

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
    private UUID userId;

    /**
     * 用户标识 (登录用户为id，匿名用户为IdentifierResolver.getCurrentUserIdentifier)
     */
    private String Identifier;

    /**
     * 用户标识 (IP哈希或设备指纹)
     */
    @NotBlank(message = "用户标识不能为空")
    private String userIdentifier;


    @NotNull(message = "工具ID不能为空")
    private UUID toolId;

    private LocalDateTime usageTime;
}