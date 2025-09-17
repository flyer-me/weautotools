package com.flyerme.weautotools.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.flyerme.weautotools.entity.Tool;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 工具数据访问层接口
 *
 * @author WeAutoTools Team
 * @version 1.0.3
 * @since 2025-09-16
 */
@Mapper
public interface ToolMapper extends BaseMapper<Tool> {

    /**
     * 检查工具名称是否存在
     *
     * @param toolName 工具名称
     * @return 是否存在
     */
    @Select("SELECT EXISTS(SELECT 1 FROM tools WHERE tool_name = #{toolName} AND deleted = 0)")
    boolean existsByToolName(String toolName);

    /**
     * 根据工具名称查询工具
     * 
     * @param toolName 工具名称
     * @return 工具实体
     */
    @Select("SELECT * FROM tools WHERE tool_name = #{toolName} AND deleted = 0")
    List<Tool> selectByToolName(String toolName);

    /**
     * 根据工具类型查询工具列表
     * 
     * @param toolType 工具类型
     * @return 工具列表
     */
    @Select("SELECT * FROM tools WHERE tool_type = #{toolType} AND deleted = 0 ORDER BY created_at DESC")
    List<Tool> selectByToolType(@Param("toolType") String toolType);

    /**
     * 查询所有活跃工具
     * 
     * @return 活跃工具列表
     */
    @Select("SELECT * FROM tools WHERE status = 'ACTIVE' AND deleted = 0 ORDER BY tool_name")
    List<Tool> selectActiveTools();

    /**
     * 根据状态查询工具列表
     * 
     * @param status 工具状态
     * @return 工具列表
     */
    @Select("SELECT * FROM tools WHERE status = #{status} AND deleted = 0 ORDER BY created_at DESC")
    List<Tool> selectByStatus(@Param("status") String status);

    /**
     * 模糊查询工具名称或描述
     * 
     * @param keyword 关键词
     * @return 工具列表
     */
    @Select("SELECT * FROM tools WHERE (tool_name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR description LIKE CONCAT('%', #{keyword}, '%')) " +
            "AND deleted = 0 ORDER BY tool_name")
    List<Tool> selectByKeyword(@Param("keyword") String keyword);

    /**
     * 根据工具名称获取工具ID
     * 
     * @param toolName 工具名称
     * @return 工具ID，不存在则返回null
     */
    @Select("SELECT id FROM tools WHERE tool_name = #{toolName} AND deleted = 0")
    Long getToolIdByName(@Param("toolName") String toolName);

    /**
     * 统计各状态的工具数量
     * 
     * @return 状态统计Map
     */
    @Select("SELECT status, COUNT(1) as count FROM tools WHERE deleted = 0 GROUP BY status")
    List<Map<String, Object>> countByStatus();

    /**
     * 统计各分类的工具数量
     * 
     * @return 分类统计Map
     */
    @Select("SELECT tool_type, COUNT(1) as count FROM tools WHERE deleted = 0 GROUP BY tool_type")
    List<Map<String, Object>> countByType();

}