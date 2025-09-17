package com.flyerme.weautotools.service.impl;

import com.flyerme.weautotools.common.BusinessException;
import com.flyerme.weautotools.entity.Tool;
import com.flyerme.weautotools.dao.ToolMapper;
import com.flyerme.weautotools.service.ToolService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
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
    public List<Tool> getByToolType(String toolType) {
        return toolMapper.selectByToolType(toolType);
    }


    @Override
    public List<Tool> getActiveTools() {
        return toolMapper.selectActiveTools();
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
    public Tool createTool(Tool tool) {
        // 检查是否已存在
        if (existsByToolName(tool.getToolName())) {
            throw new BusinessException("ToolName已存在: " + tool.getToolName());
        }
        toolMapper.insert(tool);
        log.info("创建tool成功: {}", tool.getId());
        return tool;
    }

    @Override
    public Tool updateTool(Tool tool) {
        toolMapper.updateById(tool);
        log.info("更新工具成功: {}", tool.getId());
        return tool;
    }

    @Override
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
    public Map<String, Integer> countByStatus() {
        return toolMapper.countByStatus().stream()
                .collect(Collectors.toMap(
                    item -> (String) item.get("status"),
                    item -> ((Number) item.get("count")).intValue()
                ));
    }

    @Override
    public Map<String, Integer> countByType() {
        return toolMapper.countByType().stream()
                .collect(Collectors.toMap(
                    item -> (String) item.get("category"),
                    item -> ((Number) item.get("count")).intValue()
                ));
    }

    @Override
    public List<Tool> getByToolName(String toolName) {
        return toolMapper.selectByToolName(toolName);
    }

    @Override
    public Long getToolIdByName(String toolName) {
        return 0L;
    }

    @Override
    public boolean existsByToolName(String toolName) {
        return toolMapper.existsByToolName(toolName);
    }
}