package com.flyerme.weautotools.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 点击计数器响应DTO
 *
 * @author WeAutoTools Team
 * @version 1.0.0
 * @since 2025-08-15
 */
@Data
public class ClickCounterResponse {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 计数器名称
     */
    private String counterName;

    /**
     * 计数器描述
     */
    private String description;

    /**
     * 点击次数
     */
    private Long clickCount;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 最后点击时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime lastClickTime;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updatedAt;
}
