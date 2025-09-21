package com.flyerme.weautotools.service;

import com.flyerme.weautotools.entity.Tool;

import java.util.List;

/**
 * 工具服务接口
 *
 * @author WeAutoTools Team
 * @version 1.0.3
 * @since 2025-09-16
 */
public interface ToolService {

    /**
     * 根据工具类型获取工具列表
     */
    List<Tool> getByToolType(String toolType);

    /**
     * 获取所有活跃工具
     */
    List<Tool> getActiveTools();

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
     * 根据工具名称获取工具
     *
     * @param toolName 工具名称（实际映射到tool_code字段）
     * @return 工具实体
     */

    List<Tool> getByToolName(String toolName);

    /**
     * 根据工具名称获取工具ID（兼容性方法）
     * 
     * @param toolName 工具名称
     * @return 工具ID，不存在则返回null
     */
    Long getToolIdByName(String toolName);

    /**
     * 检查工具名称是否存在
     * 
     * @param toolName 工具名称
     * @return 是否存在
     */
    boolean existsByToolName(String toolName);
}