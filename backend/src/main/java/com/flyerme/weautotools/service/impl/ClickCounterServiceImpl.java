package com.flyerme.weautotools.service.impl;

import com.flyerme.weautotools.annotation.DistributedLock;
import com.flyerme.weautotools.dto.ClickCounterRequest;
import com.flyerme.weautotools.dto.ClickCounterResponse;
import com.flyerme.weautotools.entity.ClickCounter;
import com.flyerme.weautotools.exception.BusinessException;
import com.flyerme.weautotools.mapper.ClickCounterMapper;
import com.flyerme.weautotools.service.BaseService;
import com.flyerme.weautotools.service.ClickCounterService;
import com.flyerme.weautotools.service.ClickCounterService.ClickCounterStatistics;
import com.flyerme.weautotools.util.BeanCopyUtils;
import com.flyerme.weautotools.util.DistributedLockUtil;
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
    protected int update(ClickCounter entity) {
        return clickCounterMapper.update(entity);
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
        List<ClickCounter> counters = clickCounterMapper.selectEnabled();
        return counters.stream()
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
    @DistributedLock(key = "'counter:click:' + #id", failMessage = "计数器正在被其他用户操作，请稍后重试")
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
    @DistributedLock(key = "'counter:click:' + #counterName", failMessage = "计数器正在被其他用户操作，请稍后重试")
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
        String lockKey = DistributedLockUtil.generateLockKey("counter", "reset", String.valueOf(id));

        return executeWithLock(lockKey, () -> {
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
        });
    }

}
