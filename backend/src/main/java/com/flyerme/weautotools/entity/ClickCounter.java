package com.flyerme.weautotools.entity;

import com.flyerme.weautotools.common.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;

/**
 * 点击计数器实体类
 *
 * @author WeAutoTools Team
 * @version 1.0.0
 * @since 2025-08-15
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ClickCounter extends BaseEntity {

    /**
     * 计数器名称
     */
    @NotBlank(message = "计数器名称不能为空")
    private String counterName;

    /**
     * 计数器描述
     */
    private String description;

    /**
     * 点击次数
     */
    @NotNull(message = "点击次数不能为空")
    private Long clickCount = 0L;

    /**
     * 是否启用
     */
    private Boolean enabled = true;

    /**
     * 最后点击时间
     */
    private Instant lastClickTime;
}
