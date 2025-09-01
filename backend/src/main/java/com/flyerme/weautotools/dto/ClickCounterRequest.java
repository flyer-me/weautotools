package com.flyerme.weautotools.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 点击计数器请求DTO
 *
 * @author WeAutoTools Team
 * @version 1.0.0
 * @since 2025-08-15
 */
@Data
public class ClickCounterRequest {

    /**
     * 计数器名称
     */
    @NotBlank(message = "计数器名称不能为空")
    @Size(max = 100, message = "计数器名称长度不能超过100个字符")
    private String counterName;

    /**
     * 计数器描述
     */
    @Size(max = 500, message = "计数器描述长度不能超过500个字符")
    private String description;

    /**
     * 是否启用
     */
    private Boolean enabled = true;
}
