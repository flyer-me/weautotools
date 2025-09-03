package com.flyerme.weautotools.service.impl;

import com.flyerme.weautotools.annotation.Lock;
import com.flyerme.weautotools.common.BusinessException;
import com.flyerme.weautotools.dto.ClickCounterRequest;
import com.flyerme.weautotools.dto.ClickCounterResponse;
import com.flyerme.weautotools.entity.ClickCounter;
import com.flyerme.weautotools.mapper.ClickCounterMapper;
import com.flyerme.weautotools.service.ClickCounterService;
import com.flyerme.weautotools.util.BeanCopyUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public ClickCounterServiceImpl(ClickCounterMapper clickCounterMapper) {
        super(ClickCounterResponse.class);
        this.clickCounterMapper = clickCounterMapper;
    }

    @Override
    public ClickCounterResponse getCounterByName(String counterName) {
        ClickCounter counter = clickCounterMapper.selectByName(counterName);
        if (counter == null) {
            throw new BusinessException("计数器不存在: " + counterName);
        }
        return convertToResponse(counter);
    }


    public List<ClickCounterResponse> getAllCounters() {
        return BeanCopyUtils.copyPropertiesList(list(), ClickCounterResponse.class);
    }


    public ClickCounterResponse create(ClickCounterRequest request) {
        validateCreateRequest(request);
        // 创建实体
        ClickCounter entity = BeanCopyUtils.copyProperties(request, ClickCounter.class);
        entity.setClickCount(0L);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());

        if (!save(entity)) {
            throw new BusinessException("创建计数器失败");
        }

        return convertToResponse(entity);
    }

    @Override
    public ClickCounterResponse selectById(Long id) {
        return null;
    }

    @Override
    public ClickCounterResponse updateCounter(Long id, ClickCounterRequest request) {
        return null;
    }

    @Override
    public void deleteCounter(Long id) {

    }

    @Override
    public List<ClickCounterResponse> getEnabledCounters() {
        List<ClickCounter> counters = list();
        return counters.stream()
                .filter(counter -> counter.getEnabled() != null && counter.getEnabled())
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }


    @Override
    @Transactional
    @Lock(key = "'counter:click:' + #id", failMessage = "计数器正在被其他用户操作，请稍后重试")
    public ClickCounterResponse clickCounter(Long id) {
        LocalDateTime now = LocalDateTime.now();
        int result = clickCounterMapper.incrementClickCount(id, now, now);
        if (result <= 0) {
            throw new BusinessException("点击计数失败，计数器可能不存在或已禁用");
        }

        ClickCounter counter = this.baseMapper.selectById(id);
        return convertToResponse(counter);
    }

    @Override
    @Transactional
    @Lock(key = "'counter:click:' + #counterName", failMessage = "计数器正在被其他用户操作，请稍后重试")
    public ClickCounterResponse clickCounterByName(String counterName) {
        LocalDateTime now = LocalDateTime.now();
        int result = clickCounterMapper.incrementClickCountByName(counterName, now, now);
        if (result <= 0) {
            throw new BusinessException("点击计数失败，计数器可能不存在或已禁用: " + counterName);
        }

        ClickCounter counter = clickCounterMapper.selectByName(counterName);
        log.debug("计数器点击成功: {} -> {}", counterName, counter.getClickCount());
        return convertToResponse(counter);
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
    public List<ClickCounterResponse> getCountersByPage(int page, int size) {
        List<ClickCounter> allCounters = list();
        return allCounters.stream()
                .skip((long) (page - 1) * size)
                .limit(size)
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ClickCounterResponse> getCountersByCondition(Boolean enabled, String counterName) {
        List<ClickCounter> allCounters = list();
        return allCounters.stream()
                .filter(counter -> enabled == null || counter.getEnabled().equals(enabled))
                .filter(counter -> counterName == null || counterName.isEmpty() ||
                        (counter.getCounterName() != null && counter.getCounterName().contains(counterName)))
                .map(this::convertToResponse)
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
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @Lock(key = "'counter:reset:' + #id")
    public ClickCounterResponse resetCounter(Long id) {
        ClickCounter counter = this.baseMapper.selectById(id);
        if (counter == null) {
            throw new BusinessException("计数器不存在: " + id);
        }

        // 直接更新实体并保存
        counter.setClickCount(0L);
        counter.setLastClickTime(null);
        counter.setUpdatedAt(LocalDateTime.now());

        int result = this.baseMapper.updateById(counter);
        if (result <= 0) {
            throw new BusinessException("重置计数器失败");
        }

        log.info("重置计数器成功: {}", counter.getCounterName());
        return convertToResponse(counter);
    }

    public ClickCounterResponse convertToResponse(ClickCounter entity) {
        return BeanCopyUtils.copyProperties(entity, ClickCounterResponse.class);
    }

    @Override
    protected void validateCreateRequest(ClickCounterRequest request) {
        if (request.getCounterName() == null || request.getCounterName().isEmpty()) {
            throw new BusinessException("计数器名称不能为空");
        }
        ClickCounter existing = clickCounterMapper.selectByName(request.getCounterName());
        if (existing != null) {
            throw new BusinessException("计数器名称已存在: " + request.getCounterName());
        }
    }

}
