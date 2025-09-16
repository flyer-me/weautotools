package com.flyerme.weautotools.dto;

/**
 * 点击计数统计响应DTO
 *
 * @author WeAutoTools Team
 * @version 1.0.2
 * @since 2025-09-16
 */
public record ClickCounterStatistics(long totalCounters, long enabledCounters, long totalClicks) {
}
