package com.flyerme.weautotools.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.flyerme.weautotools.dao.ToolUsageLimitMapper;
import com.flyerme.weautotools.dto.UsageLimitConfigRequest;
import com.flyerme.weautotools.dto.UsageLimitConfigResponse;
import com.flyerme.weautotools.entity.ToolUsageLimit;
import com.flyerme.weautotools.service.UsageLimitConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 使用限制配置服务实现
 *
 * @author WeAutoTools Team
 * @version 1.0.2
 * @since 2025-09-12
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UsageLimitConfigServiceImpl implements UsageLimitConfigService {

    private final ToolUsageLimitMapper toolUsageLimitMapper;

    @Override
    public List<UsageLimitConfigResponse> getAllConfigs() {
        List<ToolUsageLimit> configs = toolUsageLimitMapper.selectAllEnabled();
        return configs.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public UsageLimitConfigResponse getConfigById(Long id) {
        ToolUsageLimit config = toolUsageLimitMapper.selectById(id);
        if (config == null || config.getDeleted() != 0) {
            return null;
        }
        return convertToResponse(config);
    }

    @Override
    @Transactional
    public UsageLimitConfigResponse createConfig(UsageLimitConfigRequest request) {
        // 检查是否已存在相同配置
        LambdaQueryWrapper<ToolUsageLimit> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ToolUsageLimit::getToolName, request.getToolName())
                   .eq(ToolUsageLimit::getUserType, request.getUserType())
                   .eq(ToolUsageLimit::getLimitType, request.getLimitType())
                   .eq(ToolUsageLimit::getDeleted, 0);
        
        ToolUsageLimit existingConfig = toolUsageLimitMapper.selectOne(queryWrapper);
        if (existingConfig != null) {
            throw new IllegalArgumentException("相同配置已存在");
        }

        // 创建新配置
        ToolUsageLimit config = convertFromRequest(request);
        config.setCreatedAt(Instant.now());
        config.setUpdatedAt(Instant.now());
        
        toolUsageLimitMapper.insert(config);
        
        log.info("创建使用限制配置: {}", config);
        return convertToResponse(config);
    }

    @Override
    @Transactional
    public UsageLimitConfigResponse updateConfig(UsageLimitConfigRequest request) {
        ToolUsageLimit existingConfig = toolUsageLimitMapper.selectById(request.getId());
        if (existingConfig == null || existingConfig.getDeleted() != 0) {
            return null;
        }

        // 检查是否与其他配置冲突
        LambdaQueryWrapper<ToolUsageLimit> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ToolUsageLimit::getToolName, request.getToolName())
                   .eq(ToolUsageLimit::getUserType, request.getUserType())
                   .eq(ToolUsageLimit::getLimitType, request.getLimitType())
                   .eq(ToolUsageLimit::getDeleted, 0)
                   .ne(ToolUsageLimit::getId, request.getId());
        
        ToolUsageLimit conflictConfig = toolUsageLimitMapper.selectOne(queryWrapper);
        if (conflictConfig != null) {
            throw new IllegalArgumentException("配置与现有配置冲突");
        }

        // 更新配置
        existingConfig.setToolType(request.getToolType());
        existingConfig.setToolName(request.getToolName());
        existingConfig.setUserType(request.getUserType());
        existingConfig.setLimitType(request.getLimitType());
        existingConfig.setLimitCount(request.getLimitCount());
        existingConfig.setEnabled(request.getEnabled());
        existingConfig.setUpdatedAt(Instant.now());
        
        toolUsageLimitMapper.updateById(existingConfig);
        
        log.info("更新使用限制配置: {}", existingConfig);
        return convertToResponse(existingConfig);
    }

    @Override
    @Transactional
    public boolean deleteConfig(Long id) {
        ToolUsageLimit config = toolUsageLimitMapper.selectById(id);
        if (config == null || config.getDeleted() != 0) {
            return false;
        }

        // 逻辑删除
        config.setDeleted(1);
        config.setUpdatedAt(Instant.now());
        toolUsageLimitMapper.updateById(config);
        
        log.info("删除使用限制配置: {}", id);
        return true;
    }

    @Override
    @Transactional
    public void batchUpdateConfigs(List<UsageLimitConfigRequest> requests) {
        for (UsageLimitConfigRequest request : requests) {
            if (request.getId() != null) {
                updateConfig(request);
            } else {
                createConfig(request);
            }
        }
        log.info("批量更新使用限制配置: {} 条", requests.size());
    }

    /**
     * 转换为响应DTO
     */
    private UsageLimitConfigResponse convertToResponse(ToolUsageLimit config) {
        UsageLimitConfigResponse response = new UsageLimitConfigResponse();
        response.setId(config.getId());
        response.setToolType(config.getToolType());
        response.setToolName(config.getToolName());
        response.setUserType(config.getUserType());
        response.setLimitType(config.getLimitType());
        response.setLimitCount(config.getLimitCount());
        response.setEnabled(config.getEnabled());
        response.setCreatedAt(config.getCreatedAt());
        response.setUpdatedAt(config.getUpdatedAt());
        return response;
    }

    /**
     * 从请求DTO转换
     */
    private ToolUsageLimit convertFromRequest(UsageLimitConfigRequest request) {
        ToolUsageLimit config = new ToolUsageLimit();
        config.setToolType(request.getToolType());
        config.setToolName(request.getToolName());
        config.setUserType(request.getUserType());
        config.setLimitType(request.getLimitType());
        config.setLimitCount(request.getLimitCount());
        config.setEnabled(request.getEnabled() != null ? request.getEnabled() : true);
        return config;
    }
}