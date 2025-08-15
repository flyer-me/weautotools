package com.flyerme.weautotools.entity;

import com.flyerme.weautotools.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 点击计数器实体类
 *
 * @author WeAutoTools Team
 * @version 1.0.0
 * @since 2025-08-15
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "click_counter", indexes = {
    @Index(name = "idx_counter_name", columnList = "counter_name", unique = true)
})
public class ClickCounter extends BaseEntity {

    /**
     * 计数器名称
     */
    @NotBlank(message = "计数器名称不能为空")
    @Column(name = "counter_name", nullable = false, unique = true, length = 100)
    private String counterName;

    /**
     * 计数器描述
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * 点击次数
     */
    @NotNull(message = "点击次数不能为空")
    @Column(name = "click_count", nullable = false)
    private Long clickCount = 0L;

    /**
     * 是否启用
     */
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    /**
     * 最后点击时间
     */
    @Column(name = "last_click_time")
    private java.time.LocalDateTime lastClickTime;
}
