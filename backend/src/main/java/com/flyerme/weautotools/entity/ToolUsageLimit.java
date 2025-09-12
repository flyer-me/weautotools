package com.flyerme.weautotools.entity;

import com.flyerme.weautotools.common.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工具使用限制配置实体类
 *
 * @author WeAutoTools Team
 * @version 1.0.2
 * @since 2025-09-12
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ToolUsageLimit extends BaseEntity {

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
     * 用户类型 (ANONYMOUS/LOGIN)
     */
    @NotBlank(message = "用户类型不能为空")
    private String userType;

    /**
     * 限制类型 (DAILY/HOURLY/TOTAL)
     */
    @NotBlank(message = "限制类型不能为空")
    private String limitType;

    /**
     * 限制次数
     */
    @NotNull(message = "限制次数不能为空")
    private Integer limitCount;

    /**
     * 是否启用
     */
    private Boolean enabled = true;

    /**
     * 用户类型枚举
     */
    public enum UserType {
        ANONYMOUS, LOGIN
    }

    /**
     * 限制类型枚举
     */
    public enum LimitType {
        DAILY, HOURLY, TOTAL
    }
}