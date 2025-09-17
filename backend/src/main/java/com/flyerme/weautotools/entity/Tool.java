package com.flyerme.weautotools.entity;

import com.baomidou.mybatisplus.annotation.TableName;
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
     * 工具名称
     */
    @NotBlank(message = "工具名称不能为空")
    private String toolName;

    /**
     * 工具类型
     */
    @NotBlank(message = "工具类型不能为空")
    private String toolType;

    /**
     * 工具描述
     */
    private String description;

    /**
     * 工具状态
     */
    @NotNull(message = "工具状态不能为空")
    private String status = ToolStatus.ACTIVE.name();


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
        FILE_CONVERT,   // 文件转换
        QR_CODE,        // 二维码
        IMAGE_PROCESS,  // 图片处理
        MEDIA_PROCESS,  // 媒体处理
        DATA_EDIT,       // 数据处理
        DATA_CONVERT,   // 数据转换
        DEFAULT         // 默认工具
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

    public boolean isActive() {
        return ToolStatus.ACTIVE.name().equals(this.status);
    }

}