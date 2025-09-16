package com.flyerme.weautotools.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.flyerme.weautotools.entity.ToolUsageLimit;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 工具使用限制配置Mapper接口
 *
 * @author WeAutoTools Team
 * @version 1.0.3
 * @since 2025-09-16
 */
@Mapper
public interface ToolUsageLimitMapper extends BaseMapper<ToolUsageLimit> {

    /**
     * 根据工具ID和用户类型查询限制配置
     */
    @Select("SELECT * FROM tool_usage_limits WHERE tool_id = #{toolId} AND user_type = #{userType} " +
            "AND enabled = true AND deleted = 0")
    List<ToolUsageLimit> selectByToolIdAndUserType(@Param("toolId") Long toolId, 
                                                   @Param("userType") String userType);

    /**
     * 根据工具代码和用户类型查询限制配置（关联查询）
     */
    @Select("SELECT tul.* FROM tool_usage_limits tul " +
            "INNER JOIN tools t ON tul.tool_id = t.id " +
            "WHERE t.tool_code = #{toolCode} AND tul.user_type = #{userType} " +
            "AND tul.enabled = true AND tul.deleted = 0 AND t.deleted = 0")
    List<ToolUsageLimit> selectByToolCodeAndUserType(@Param("toolCode") String toolCode, 
                                                     @Param("userType") String userType);

    /**
     * 根据工具类型和用户类型查询默认限制配置（关联查询）
     */
    @Select("SELECT tul.* FROM tool_usage_limits tul " +
            "INNER JOIN tools t ON tul.tool_id = t.id " +
            "WHERE t.tool_type = #{toolType} AND tul.user_type = #{userType} " +
            "AND tul.enabled = true AND tul.deleted = 0 AND t.deleted = 0")
    List<ToolUsageLimit> selectByToolTypeAndUserType(@Param("toolType") String toolType, 
                                                     @Param("userType") String userType);

    /**
     * 查询默认限制配置（DEFAULT类型工具）
     */
    @Select("SELECT tul.* FROM tool_usage_limits tul " +
            "INNER JOIN tools t ON tul.tool_id = t.id " +
            "WHERE t.tool_type = 'DEFAULT' AND tul.user_type = #{userType} " +
            "AND tul.enabled = true AND tul.deleted = 0 AND t.deleted = 0")
    List<ToolUsageLimit> selectDefaultLimits(@Param("userType") String userType);

    /**
     * 查询所有启用的限制配置（包含工具信息）
     */
    @Select("SELECT tul.*, t.tool_code, t.tool_name, t.tool_type, t.category " +
            "FROM tool_usage_limits tul " +
            "INNER JOIN tools t ON tul.tool_id = t.id " +
            "WHERE tul.enabled = true AND tul.deleted = 0 AND t.deleted = 0 " +
            "ORDER BY t.tool_name, tul.user_type")
    List<java.util.Map<String, Object>> selectAllEnabledWithToolInfo();

    /**
     * 兼容性方法：根据工具名称和用户类型查询限制配置（旧接口）
     * @deprecated 建议使用 selectByToolCodeAndUserType
     */
    @Deprecated
    default List<ToolUsageLimit> selectByToolNameAndUserType(String toolName, String userType) {
        return selectByToolCodeAndUserType(toolName, userType);
    }

    /**
     * 兼容性方法：查询所有启用的限制配置（旧接口）
     * @deprecated 建议使用 selectAllEnabledWithToolInfo
     */
    @Deprecated
    @Select("SELECT * FROM tool_usage_limits WHERE enabled = true AND deleted = 0 ORDER BY tool_name, user_type")
    List<ToolUsageLimit> selectAllEnabled();
}