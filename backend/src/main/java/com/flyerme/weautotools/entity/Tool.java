package com.flyerme.weautotools.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.flyerme.weautotools.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 工具实体类
 *
 * @author WeAutoTools Team
 * @version 1.0.3
 * @since 2025-09-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tools")
public class Tool extends BaseEntity {

    /**
     * 工具代码（唯一标识）
     */
    @NotBlank(message = "工具代码不能为空")
    @TableField("tool_code")
    private String toolCode;

    /**
     * 工具名称
     */
    @NotBlank(message = "工具名称不能为空")
    @TableField("tool_name")
    private String toolName;

    /**
     * 工具类型
     */
    @NotBlank(message = "工具类型不能为空")
    @TableField("tool_type")
    private String toolType;

    /**
     * 工具描述
     */
    @TableField("description")
    private String description;

    /**
     * 工具分类
     */
    @TableField("category")
    private String category;

    /**
     * 工具状态
     */
    @NotNull(message = "工具状态不能为空")
    @TableField("status")
    private String status = ToolStatus.ACTIVE.name();

    /**
     * 是否为前端工具
     */
    @TableField("is_frontend")
    private Boolean isFrontend = false;

    /**
     * 工具状态枚举
     */
    public enum ToolStatus {
        ACTIVE,      // 活跃
        INACTIVE,    // 非活跃
        DEPRECATED   // 已弃用
    }

    /**
     * 工具类型枚举
     */
    public enum ToolType {
        QR_CODE,        // 二维码工具
        IMAGE_PROCESS,  // 图片处理工具
        DATA_CONVERT,   // 数据转换工具
        DEFAULT         // 默认工具
    }

    /**
     * 工具分类枚举
     */
    public enum ToolCategory {
        QRCODE,    // 二维码
        IMAGE,     // 图片处理
        DATA,      // 数据处理
        BASE       // 基础工具
    }

    /**
     * 便捷方法：设置工具状态（使用枚举）
     */
    public void setStatus(ToolStatus status) {
        this.status = status.name();
    }

    /**
     * 便捷方法：获取工具状态枚举
     */
    public ToolStatus getStatusEnum() {
        try {
            return ToolStatus.valueOf(this.status);
        } catch (IllegalArgumentException e) {
            return ToolStatus.ACTIVE; // 默认状态
        }
    }

    /**
     * 便捷方法：设置工具类型（使用枚举）
     */
    public void setToolType(ToolType toolType) {
        this.toolType = toolType.name();
    }

    /**
     * 便捷方法：获取工具类型枚举
     */
    public ToolType getToolTypeEnum() {
        try {
            return ToolType.valueOf(this.toolType);
        } catch (IllegalArgumentException e) {
            return ToolType.DEFAULT; // 默认类型
        }
    }

    /**
     * 便捷方法：设置工具分类（使用枚举）
     */
    public void setCategory(ToolCategory category) {
        this.category = category.name().toLowerCase();
    }

    /**
     * 便捷方法：获取工具分类枚举
     */
    public ToolCategory getCategoryEnum() {
        try {
            return ToolCategory.valueOf(this.category.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            return ToolCategory.BASE; // 默认分类
        }
    }

    /**
     * 判断是否为活跃状态
     */
    public boolean isActive() {
        return ToolStatus.ACTIVE.name().equals(this.status);
    }

    /**
     * 判断是否为前端工具
     */
    public boolean isFrontendTool() {
        return Boolean.TRUE.equals(this.isFrontend);
    }
}