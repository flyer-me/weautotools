package com.flyerme.weautotools.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 工具使用限制配置请求DTO
 *
 * @author WeAutoTools Team
 * @version 1.0.2
 * @since 2025-09-12
 */
@Data
public class UsageLimitConfigRequest {

    /**
     * 配置ID（更新时使用）
     */
    private Long id;

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
}