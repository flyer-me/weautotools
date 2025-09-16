package com.flyerme.weautotools.service;

import com.flyerme.weautotools.entity.Tool;
import java.util.List;
import java.util.Optional;

/**
 * 工具服务接口
 *
 * @author WeAutoTools Team
 * @version 1.0.3
 * @since 2025-09-16
 */
public interface ToolService {

    /**
     * 根据工具代码获取工具
     */
    Optional<Tool> getByToolCode(String toolCode);

    /**
     * 根据工具类型获取工具列表
     */
    List<Tool> getByToolType(String toolType);

    /**
     * 根据工具分类获取工具列表
     */
    List<Tool> getByCategory(String category);

    /**
     * 获取所有活跃工具
     */
    List<Tool> getActiveTools();

    /**
     * 获取前端工具列表
     */
    List<Tool> getFrontendTools();

    /**
     * 获取后端工具列表
     */
    List<Tool> getBackendTools();

    /**
     * 根据状态获取工具列表
     */
    List<Tool> getByStatus(String status);

    /**
     * 根据关键词搜索工具
     */
    List<Tool> searchByKeyword(String keyword);

    /**
     * 创建工具
     */
    Tool createTool(Tool tool);

    /**
     * 更新工具
     */
    Tool updateTool(Tool tool);

    /**
     * 根据ID删除工具（逻辑删除）
     */
    boolean deleteTool(Long id);

    /**
     * 根据工具代码获取工具ID
     */
    Long getToolIdByCode(String toolCode);

    /**
     * 检查工具代码是否存在
     */
    boolean existsByToolCode(String toolCode);

    /**
     * 统计各状态的工具数量
     */
    java.util.Map<String, Integer> countByStatus();

    /**
     * 统计各分类的工具数量
     */
    java.util.Map<String, Integer> countByCategory();
}