package com.flyerme.weautotools.service;

import com.flyerme.weautotools.dto.ClickCounterRequest;
import com.flyerme.weautotools.dto.ClickCounterResponse;

import java.util.List;

/**
 * 点击计数器服务接口
 *
 * @author WeAutoTools Team
 * @version 1.0.0
 * @since 2025-08-15
 */
public interface ClickCounterService {

    /**
     * 创建计数器
     */
    ClickCounterResponse createCounter(ClickCounterRequest request);

    /**
     * 根据ID获取计数器
     */
    ClickCounterResponse getCounterById(Long id);

    /**
     * 根据名称获取计数器
     */
    ClickCounterResponse getCounterByName(String counterName);

    /**
     * 获取所有计数器
     */
    List<ClickCounterResponse> getAllCounters();

    /**
     * 获取所有启用的计数器
     */
    List<ClickCounterResponse> getEnabledCounters();

    /**
     * 更新计数器
     */
    ClickCounterResponse updateCounter(Long id, ClickCounterRequest request);

    /**
     * 删除计数器
     */
    void deleteCounter(Long id);

    /**
     * 点击计数（根据ID）
     */
    ClickCounterResponse clickCounter(Long id);

    /**
     * 点击计数（根据名称）
     */
    ClickCounterResponse clickCounterByName(String counterName);

    /**
     * 获取计数器统计信息
     */
    ClickCounterStatistics getStatistics();

    /**
     * 分页查询计数器
     */
    List<ClickCounterResponse> getCountersByPage(int page, int size);

    /**
     * 根据条件查询计数器
     */
    List<ClickCounterResponse> getCountersByCondition(Boolean enabled, String counterName);

    /**
     * 获取点击数最多的计数器
     */
    List<ClickCounterResponse> getTopCountersByClicks(int limit);

    /**
     * 重置计数器点击数
     */
    ClickCounterResponse resetCounter(Long id);

    /**
     * 计数器统计信息
     */
    class ClickCounterStatistics {
        private long totalCounters;
        private long enabledCounters;
        private long totalClicks;

        public ClickCounterStatistics(long totalCounters, long enabledCounters, long totalClicks) {
            this.totalCounters = totalCounters;
            this.enabledCounters = enabledCounters;
            this.totalClicks = totalClicks;
        }

        // Getters
        public long getTotalCounters() { return totalCounters; }
        public long getEnabledCounters() { return enabledCounters; }
        public long getTotalClicks() { return totalClicks; }
    }
}
