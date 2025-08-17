package com.flyerme.weautotools.service.impl;

import com.flyerme.weautotools.dto.ClickCounterRequest;
import com.flyerme.weautotools.dto.ClickCounterResponse;
import com.flyerme.weautotools.entity.ClickCounter;
import com.flyerme.weautotools.exception.BusinessException;
import com.flyerme.weautotools.mapper.ClickCounterMapper;

import com.flyerme.weautotools.service.ClickCounterService;
import com.flyerme.weautotools.util.BeanCopyUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 点击计数器服务实现类
 *
 * @author WeAutoTools Team
 * @version 1.0.0
 * @since 2025-08-15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClickCounterServiceImpl implements ClickCounterService {

    private final ClickCounterMapper clickCounterMapper;

    @Override
    @Transactional
    public ClickCounterResponse createCounter(ClickCounterRequest request) {
        // 检查名称是否已存在
        ClickCounter existing = clickCounterMapper.selectByName(request.getCounterName());
        if (existing != null) {
            throw new BusinessException("计数器名称已存在: " + request.getCounterName());
        }

        // 创建新计数器
        ClickCounter counter = new ClickCounter();
        counter.setCounterName(request.getCounterName());
        counter.setDescription(request.getDescription());
        counter.setEnabled(request.getEnabled());
        counter.setClickCount(0L);
        counter.setCreatedAt(LocalDateTime.now());
        counter.setUpdatedAt(LocalDateTime.now());
        counter.setDeleted(0);
        counter.setVersion(0);

        int result = clickCounterMapper.insert(counter);
        if (result <= 0) {
            throw new BusinessException("创建计数器失败");
        }

        log.info("创建计数器成功: {}", request.getCounterName());
        return convertToResponse(counter);
    }

    @Override
    public ClickCounterResponse getCounterById(Long id) {
        ClickCounter counter = clickCounterMapper.selectById(id);
        if (counter == null) {
            throw new BusinessException("计数器不存在: " + id);
        }
        return convertToResponse(counter);
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
        List<ClickCounter> counters = clickCounterMapper.selectAll();
        return counters.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ClickCounterResponse> getEnabledCounters() {
        List<ClickCounter> counters = clickCounterMapper.selectEnabled();
        return counters.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ClickCounterResponse updateCounter(Long id, ClickCounterRequest request) {
        ClickCounter counter = clickCounterMapper.selectById(id);
        if (counter == null) {
            throw new BusinessException("计数器不存在: " + id);
        }

        // 检查名称是否被其他计数器使用
        if (!counter.getCounterName().equals(request.getCounterName())) {
            long count = clickCounterMapper.countByNameExcludeId(request.getCounterName(), id);
            if (count > 0) {
                throw new BusinessException("计数器名称已存在: " + request.getCounterName());
            }
        }

        // 更新计数器信息
        counter.setCounterName(request.getCounterName());
        counter.setDescription(request.getDescription());
        counter.setEnabled(request.getEnabled());
        counter.setUpdatedAt(LocalDateTime.now());

        int result = clickCounterMapper.update(counter);
        if (result <= 0) {
            throw new BusinessException("更新计数器失败，可能是并发冲突");
        }

        log.info("更新计数器成功: {}", request.getCounterName());
        return convertToResponse(counter);
    }

    @Override
    @Transactional
    public void deleteCounter(Long id) {
        ClickCounter counter = clickCounterMapper.selectById(id);
        if (counter == null) {
            throw new BusinessException("计数器不存在: " + id);
        }

        int result = clickCounterMapper.deleteById(id, LocalDateTime.now());
        if (result <= 0) {
            throw new BusinessException("删除计数器失败");
        }

        log.info("删除计数器成功: {}", counter.getCounterName());
    }

    @Override
    @Transactional
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
        long totalCounters = clickCounterMapper.count();
        long enabledCounters = clickCounterMapper.countEnabled();
        long totalClicks = clickCounterMapper.sumTotalClicks();

        return new ClickCounterStatistics(totalCounters, enabledCounters, totalClicks);
    }

    @Override
    public List<ClickCounterResponse> getCountersByPage(int page, int size) {
        long offset = (long) (page - 1) * size;
        List<ClickCounter> counters = clickCounterMapper.selectByPage(offset, size);
        return counters.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ClickCounterResponse> getCountersByCondition(Boolean enabled, String counterName) {
        List<ClickCounter> counters = clickCounterMapper.selectByCondition(enabled, counterName);
        return counters.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ClickCounterResponse> getTopCountersByClicks(int limit) {
        List<ClickCounter> counters = clickCounterMapper.selectTopByClicks(limit);
        return counters.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ClickCounterResponse resetCounter(Long id) {
        ClickCounter counter = clickCounterMapper.selectById(id);
        if (counter == null) {
            throw new BusinessException("计数器不存在: " + id);
        }

        int result = clickCounterMapper.resetClickCount(id, LocalDateTime.now());
        if (result <= 0) {
            throw new BusinessException("重置计数器失败");
        }

        // 重新查询更新后的数据
        counter = clickCounterMapper.selectById(id);
        log.info("重置计数器成功: {}", counter.getCounterName());
        return convertToResponse(counter);
    }

    /**
     * 转换为响应DTO
     */
    private ClickCounterResponse convertToResponse(ClickCounter counter) {
        return BeanCopyUtils.copyProperties(counter, ClickCounterResponse.class);
    }
}
