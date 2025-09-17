package com.flyerme.weautotools.service;

import com.flyerme.weautotools.common.PageResult;
import com.flyerme.weautotools.dto.ClickCounterRequest;
import com.flyerme.weautotools.dto.ClickCounterResponse;
import com.flyerme.weautotools.dto.ClickCounterStatistics;
import com.flyerme.weautotools.entity.ClickCounter;

import java.util.List;

/**
 * 点击计数器服务接口
 *
 * @author WeAutoTools Team
 * @version 1.0.0
 * @since 2025-08-15
 */
public interface ClickCounterService extends BaseService<ClickCounter, ClickCounterRequest, ClickCounterResponse> {


    ClickCounterResponse getCounterByName(String counterName);

    List<ClickCounterResponse> getEnabledCounters();

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

    PageResult<ClickCounterResponse> getCountersByPageResult(int page, int size);

    List<ClickCounterResponse> getCountersByCondition(Boolean enabled, String counterName);

    List<ClickCounterResponse> getTopCountersByClicks(int limit);

    /**
     * 重置计数器点击数
     */
    ClickCounterResponse resetCounter(Long id);

}
