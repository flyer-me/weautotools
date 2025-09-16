package com.flyerme.weautotools.service.impl;

import com.flyerme.weautotools.entity.Tool;
import com.flyerme.weautotools.mapper.ToolMapper;
import com.flyerme.weautotools.service.ToolService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 工具服务实现类
 *
 * @author WeAutoTools Team
 * @version 1.0.3
 * @since 2025-09-16
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ToolServiceImpl implements ToolService {

    private final ToolMapper toolMapper;

    @Override
    public Optional<Tool> getByToolCode(String toolCode) {
        return Optional.ofNullable(toolMapper.selectByToolCode(toolCode));
    }

    @Override
    public List<Tool> getByToolType(String toolType) {
        return toolMapper.selectByToolType(toolType);
    }

    @Override
    public List<Tool> getByCategory(String category) {
        return toolMapper.selectByCategory(category);
    }

    @Override
    public List<Tool> getActiveTools() {
        return toolMapper.selectActiveTools();
    }

    @Override
    public List<Tool> getFrontendTools() {
        return toolMapper.selectFrontendTools();
    }

    @Override
    public List<Tool> getBackendTools() {
        return toolMapper.selectBackendTools();
    }

    @Override
    public List<Tool> getByStatus(String status) {
        return toolMapper.selectByStatus(status);
    }

    @Override
    public List<Tool> searchByKeyword(String keyword) {
        return toolMapper.selectByKeyword(keyword);
    }

    @Override
    @Transactional
    public Tool createTool(Tool tool) {
        // 检查工具代码是否已存在
        if (existsByToolCode(tool.getToolCode())) {
            throw new IllegalArgumentException("工具代码已存在: " + tool.getToolCode());
        }
        
        toolMapper.insert(tool);
        log.info("创建工具成功: {}", tool.getToolCode());
        return tool;
    }

    @Override
    @Transactional
    public Tool updateTool(Tool tool) {
        toolMapper.updateById(tool);
        log.info("更新工具成功: {}", tool.getToolCode());
        return tool;
    }

    @Override
    @Transactional
    public boolean deleteTool(Long id) {
        Tool tool = toolMapper.selectById(id);
        if (tool != null) {
            tool.setDeleted(1);
            toolMapper.updateById(tool);
            log.info("删除工具成功: ID={}", id);
            return true;
        }
        return false;
    }

    @Override
    public Long getToolIdByCode(String toolCode) {
        return getByToolCode(toolCode)
                .map(Tool::getId)
                .orElse(null);
    }

    @Override
    public boolean existsByToolCode(String toolCode) {
        return toolMapper.existsByToolCode(toolCode) > 0;
    }

    @Override
    public Map<String, Integer> countByStatus() {
        return toolMapper.countByStatus().stream()
                .collect(Collectors.toMap(
                    item -> (String) item.get("status"),
                    item -> ((Number) item.get("count")).intValue()
                ));
    }

    @Override
    public Map<String, Integer> countByCategory() {
        return toolMapper.countByCategory().stream()
                .collect(Collectors.toMap(
                    item -> (String) item.get("category"),
                    item -> ((Number) item.get("count")).intValue()
                ));
    }
}