package com.flyerme.weautotools.dto;

import java.time.Instant;

/**
 * 点击计数器响应DTO
 *
 * @author WeAutoTools Team
 * @version 1.0.0
 * @since 2025-08-15
 */
public record ClickCounterResponse
        (Long id, String counterName, String description,
         Long clickCount, Boolean enabled, Instant lastClickTime,
         Instant createdAt, Instant updatedAt) {}