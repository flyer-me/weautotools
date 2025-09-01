package com.flyerme.weautotools.service.impl;

import com.flyerme.weautotools.annotation.Lock;
import com.flyerme.weautotools.dto.ClickCounterRequest;
import com.flyerme.weautotools.dto.ClickCounterResponse;
import com.flyerme.weautotools.entity.ClickCounter;
import com.flyerme.weautotools.exception.BusinessException;
import com.flyerme.weautotools.mapper.ClickCounterMapper;
import com.flyerme.weautotools.service.BaseService;
import com.flyerme.weautotools.service.ClickCounterService;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class ClickCounterServiceImpl extends BaseService<ClickCounter, ClickCounterRequest, ClickCounterResponse>
        implements ClickCounterService {

    private final ClickCounterMapper clickCounterMapper;

    // ========== BaseService抽象方法实现 ==========

    @Override
    protected Class<ClickCounter> getEntityClass() {
        return ClickCounter.class;
    }

    @Override
    protected Class<ClickCounterResponse> getResponseClass() {
        return ClickCounterResponse.class;
    }

    @Override
    protected String getEntityName() {
        return "计数器";
    }

    @Override
    protected ClickCounter selectById(Long id) {
        return clickCounterMapper.selectById(id);
    }

    @Override
    protected int insert(ClickCounter entity) {
        return clickCounterMapper.insert(entity);
    }

    @Override
    protected int updateById(ClickCounter entity) {
        return clickCounterMapper.updateById(entity);
    }

    @Override
    protected int deleteById(Long id, LocalDateTime deleteTime) {
        return clickCounterMapper.deleteById(id, deleteTime);
    }

    @Override
    protected List<ClickCounter> selectAll() {
        return clickCounterMapper.selectAll();
    }

    // ========== 业务规则验证重写 ==========

    @Override
    protected void checkCreateBusinessRules(ClickCounterRequest request) {
        // 检查名称是否已存在
        ClickCounter existing = clickCounterMapper.selectByName(request.getCounterName());
        if (existing != null) {
            throw new BusinessException("计数器名称已存在: " + request.getCounterName());
        }
    }

    @Override
    protected void checkUpdateBusinessRules(Long id, ClickCounterRequest request, ClickCounter entity) {
        // 检查名称是否被其他计数器使用
        if (!entity.getCounterName().equals(request.getCounterName())) {
            long count = clickCounterMapper.countByNameExcludeId(request.getCounterName(), id);
            if (count > 0) {
                throw new BusinessException("计数器名称已存在: " + request.getCounterName());
            }
        }
    }

    @Override
    protected void setCreateDefaults(ClickCounter entity) {
        super.setCreateDefaults(entity);
        entity.setClickCount(0L);
    }

    // ========== 原有业务方法重构 ==========

    @Override
    @Transactional
    public ClickCounterResponse createCounter(ClickCounterRequest request) {
        return create(request);
    }

    @Override
    public ClickCounterResponse getCounterById(Long id) {
        return getById(id);
    }

    @Override
    public ClickCounterResponse getCounterByName(String counterName) {
        ClickCounter counter = clickCounterMapper.selectByName(counterName);
        if (counter == null) {
            throw new BusinessException("计数器不存在: " + counterName);
        }
        return convertToResponse(counter);
    }

    @Override
    public List<ClickCounterResponse> getAllCounters() {
        return getAll();
    }

    @Override
    public List<ClickCounterResponse> getEnabledCounters() {
        List<ClickCounter> counters = clickCounterMapper.selectAll();
        return counters.stream()
                .filter(counter -> counter.getEnabled() != null && counter.getEnabled())
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ClickCounterResponse updateCounter(Long id, ClickCounterRequest request) {
        return update(id, request);
    }

    @Override
    @Transactional
    public void deleteCounter(Long id) {
        delete(id);
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

        ClickCounter counter = clickCounterMapper.selectById(id);
        log.debug("计数器点击成功: {} -> {}", counter.getCounterName(), counter.getClickCount());
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
        List<ClickCounter> allCounters = clickCounterMapper.selectAll();
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
        List<ClickCounter> allCounters = clickCounterMapper.selectAll();
        return allCounters.stream()
                .skip((long) (page - 1) * size)
                .limit(size)
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ClickCounterResponse> getCountersByCondition(Boolean enabled, String counterName) {
        List<ClickCounter> allCounters = clickCounterMapper.selectAll();
        return allCounters.stream()
                .filter(counter -> enabled == null || counter.getEnabled().equals(enabled))
                .filter(counter -> counterName == null || counterName.isEmpty() ||
                        (counter.getCounterName() != null && counter.getCounterName().contains(counterName)))
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ClickCounterResponse> getTopCountersByClicks(int limit) {
        List<ClickCounter> allCounters = clickCounterMapper.selectAll();
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
        ClickCounter counter = clickCounterMapper.selectById(id);
        if (counter == null) {
            throw new BusinessException("计数器不存在: " + id);
        }

        // 直接更新实体并保存
        counter.setClickCount(0L);
        counter.setLastClickTime(null);
        counter.setUpdatedAt(LocalDateTime.now());

        int result = clickCounterMapper.updateById(counter);
        if (result <= 0) {
            throw new BusinessException("重置计数器失败");
        }

        log.info("重置计数器成功: {}", counter.getCounterName());
        return convertToResponse(counter);
    }

}
