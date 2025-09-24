package com.flyerme.weautotools.service.impl;

import com.flyerme.weautotools.annotation.Lock;
import com.flyerme.weautotools.common.BusinessException;
import com.flyerme.weautotools.common.PageResult;
import com.flyerme.weautotools.dao.ClickCounterMapper;
import com.flyerme.weautotools.dto.ClickCounterRequest;
import com.flyerme.weautotools.dto.ClickCounterResponse;
import com.flyerme.weautotools.dto.ClickCounterStatistics;
import com.flyerme.weautotools.entity.ClickCounter;
import com.flyerme.weautotools.mapper.ClickCounterConverter;
import com.flyerme.weautotools.service.ClickCounterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 点击计数器服务实现类
 * 继承BaseService，提供通用的CRUD操作和分布式锁支持
 *
 * @author WeAutoTools Team
 * @version 1.0.0
 * @since 2025-08-15
 */
@Slf4j
@Service
public class ClickCounterServiceImpl extends BaseServiceImpl<ClickCounter, ClickCounterRequest, ClickCounterResponse>
        implements ClickCounterService {

    private final ClickCounterMapper clickCounterMapper;
    private final ClickCounterConverter converter;

    public ClickCounterServiceImpl(ClickCounterMapper mapper, ClickCounterConverter converter) {
        super(converter);
        this.clickCounterMapper = mapper;
        this.converter = converter;
    }

    @Override
    public ClickCounterResponse getCounterByName(String counterName) {
        ClickCounter counter = clickCounterMapper.selectByName(counterName);
        if (counter == null) {
            throw new BusinessException("计数器不存在: " + counterName);
        }
        return converter.toResponse(counter);
    }

    @Override
    public List<ClickCounterResponse> getEnabledCounters() {
        List<ClickCounter> counters = list();
        return counters.stream()
                .filter(counter -> counter.getEnabled() != null && counter.getEnabled())
                .map(converter::toResponse)
                .toList();
    }


    @Override
    public ClickCounterResponse clickCounter(String id) {
        LocalDateTime now = LocalDateTime.now();
        int result = clickCounterMapper.incrementClickCount(id, now, now);
        if (result <= 0) {
            throw new BusinessException("点击计数失败，计数器可能不存在或已禁用");
        }

        ClickCounter counter = this.baseMapper.selectById(id);
        return converter.toResponse(counter);
    }

    @Override
    public ClickCounterResponse clickCounterByName(String counterName) {
        LocalDateTime now = LocalDateTime.now();
        int result = clickCounterMapper.incrementClickCountByName(counterName, now, now);
        if (result <= 0) {
            throw new BusinessException("点击计数失败，计数器可能不存在或已禁用: " + counterName);
        }

        ClickCounter counter = clickCounterMapper.selectByName(counterName);
        log.debug("计数器点击成功: {} -> {}", counterName, counter.getClickCount());
        return converter.toResponse(counter);
    }

    @Override
    public ClickCounterStatistics getStatistics() {
        List<ClickCounter> allCounters = list();
        long totalCounters = allCounters.size();
        long enabledCounters = allCounters.stream()
                .filter(counter -> counter.getEnabled() != null && counter.getEnabled())
                .count();
        long totalClicks = allCounters.stream()
                .mapToLong(counter -> counter.getClickCount() != null ? counter.getClickCount() : 0L)
                .sum();

        return new ClickCounterStatistics(totalCounters, enabledCounters, totalClicks);
    }

    @Override
    public PageResult<ClickCounterResponse> getCountersByPageResult(int page, int size) {
        List<ClickCounter> allCounters = list();
        long total = allCounters.size();
        
        List<ClickCounterResponse> records = allCounters.stream()
                .skip((long) (page - 1) * size)
                .limit(size)
                .map(converter::toResponse)
                .toList();
        
        return PageResult.of(records, total, (long) size, (long) page);
    }

    @Override
    public List<ClickCounterResponse> getCountersByCondition(Boolean enabled, String counterName) {
        List<ClickCounter> allCounters = list();
        return allCounters.stream()
                .filter(counter -> enabled == null || counter.getEnabled().equals(enabled))
                .filter(counter -> counterName == null || counterName.isEmpty() ||
                        (counter.getCounterName() != null && counter.getCounterName().contains(counterName)))
                .map(converter::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ClickCounterResponse> getTopCountersByClicks(int limit) {
        List<ClickCounter> allCounters = list();
        return allCounters.stream()
                .sorted((c1, c2) -> Long.compare(
                        c2.getClickCount() != null ? c2.getClickCount() : 0L,
                        c1.getClickCount() != null ? c1.getClickCount() : 0L))
                .limit(limit)
                .map(converter::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @Lock(key = "'counter:reset:' + #id")
    public ClickCounterResponse resetCounter(String id) {
        ClickCounter counter = this.baseMapper.selectById(id);
        if (counter == null) {
            throw new BusinessException("计数器不存在: " + id);
        }

        // 直接更新实体并保存
        counter.setClickCount(0L);
        counter.setLastClickTime(null);
        counter.setUpdatedAt(Instant.now());

        int result = this.baseMapper.updateById(counter);
        if (result <= 0) {
            throw new BusinessException("重置计数器失败");
        }

        log.info("重置计数器成功: {}", counter.getCounterName());
        return converter.toResponse(counter);
    }

    @Override
    protected void validateCreateRequest(ClickCounterRequest request) {
        ClickCounter existing = clickCounterMapper.selectByName(request.getCounterName());
        if (existing != null) {
            throw new BusinessException("计数器名称已存在: " + request.getCounterName());
        }
    }

}
