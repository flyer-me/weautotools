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
 * @version 1.0.2
 * @since 2025-09-12
 */
@Mapper
public interface ToolUsageLimitMapper extends BaseMapper<ToolUsageLimit> {

    /**
     * 根据工具名称和用户类型查询限制配置
     */
    @Select("SELECT * FROM tool_usage_limits WHERE tool_name = #{toolName} AND user_type = #{userType} " +
            "AND enabled = true AND deleted = 0")
    List<ToolUsageLimit> selectByToolNameAndUserType(@Param("toolName") String toolName, 
                                                     @Param("userType") String userType);

    /**
     * 根据工具类型和用户类型查询默认限制配置
     */
    @Select("SELECT * FROM tool_usage_limits WHERE tool_type = #{toolType} AND user_type = #{userType} " +
            "AND enabled = true AND deleted = 0")
    List<ToolUsageLimit> selectByToolTypeAndUserType(@Param("toolType") String toolType, 
                                                     @Param("userType") String userType);

    /**
     * 查询默认限制配置
     */
    @Select("SELECT * FROM tool_usage_limits WHERE tool_type = 'DEFAULT' AND user_type = #{userType} " +
            "AND enabled = true AND deleted = 0")
    List<ToolUsageLimit> selectDefaultLimits(@Param("userType") String userType);

    /**
     * 查询所有启用的限制配置
     */
    @Select("SELECT * FROM tool_usage_limits WHERE enabled = true AND deleted = 0 ORDER BY tool_name, user_type")
    List<ToolUsageLimit> selectAllEnabled();
}