package com.flyerme.weautotools.mapper;

import com.flyerme.weautotools.entity.Tool;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

/**
 * 工具Mapper接口
 *
 * @author WeAutoTools Team
 * @version 1.0.3
 * @since 2025-09-16
 */
@Mapper
public interface ToolMapper extends BaseMapper<Tool> {

    /**
     * 根据工具代码查询工具
     */
    @Select("SELECT * FROM tools WHERE tool_code = #{toolCode} AND deleted = 0")
    Tool selectByToolCode(@Param("toolCode") String toolCode);

    /**
     * 根据工具类型查询工具列表
     */
    @Select("SELECT * FROM tools WHERE tool_type = #{toolType} AND deleted = 0 ORDER BY created_at DESC")
    List<Tool> selectByToolType(@Param("toolType") String toolType);

    /**
     * 根据工具分类查询工具列表
     */
    @Select("SELECT * FROM tools WHERE category = #{category} AND deleted = 0 ORDER BY created_at DESC")
    List<Tool> selectByCategory(@Param("category") String category);

    /**
     * 查询所有活跃工具
     */
    @Select("SELECT * FROM tools WHERE status = 'ACTIVE' AND deleted = 0 ORDER BY category, tool_name")
    List<Tool> selectActiveTools();

    /**
     * 查询前端工具
     */
    @Select("SELECT * FROM tools WHERE is_frontend = true AND deleted = 0 ORDER BY category, tool_name")
    List<Tool> selectFrontendTools();

    /**
     * 查询后端工具
     */
    @Select("SELECT * FROM tools WHERE is_frontend = false AND deleted = 0 ORDER BY category, tool_name")
    List<Tool> selectBackendTools();

    /**
     * 根据状态查询工具列表
     */
    @Select("SELECT * FROM tools WHERE status = #{status} AND deleted = 0 ORDER BY created_at DESC")
    List<Tool> selectByStatus(@Param("status") String status);

    /**
     * 模糊查询工具名称或描述
     */
    @Select("SELECT * FROM tools WHERE (tool_name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR description LIKE CONCAT('%', #{keyword}, '%')) " +
            "AND deleted = 0 ORDER BY tool_name")
    List<Tool> selectByKeyword(@Param("keyword") String keyword);

    /**
     * 检查工具代码是否存在
     */
    @Select("SELECT COUNT(1) FROM tools WHERE tool_code = #{toolCode} AND deleted = 0")
    int existsByToolCode(@Param("toolCode") String toolCode);

    /**
     * 统计各状态的工具数量
     */
    @Select("SELECT status, COUNT(1) as count FROM tools WHERE deleted = 0 GROUP BY status")
    List<java.util.Map<String, Object>> countByStatus();

    /**
     * 统计各分类的工具数量
     */
    @Select("SELECT category, COUNT(1) as count FROM tools WHERE deleted = 0 GROUP BY category")
    List<java.util.Map<String, Object>> countByCategory();
}