package com.flyerme.weautotools.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.flyerme.weautotools.entity.ToolUsageRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 工具使用记录Mapper接口
 *
 * @author WeAutoTools Team
 * @version 1.0.2
 * @since 2025-09-12
 */
@Mapper
public interface ToolUsageRecordMapper extends BaseMapper<ToolUsageRecord> {

    /**
     * 统计指定时间范围内的使用次数
     */
    @Select("SELECT COUNT(*) FROM tool_usage_records WHERE user_identifier = #{userIdentifier} " +
            "AND tool_name = #{toolName} AND usage_time >= #{startTime} AND usage_time <= #{endTime} " +
            "AND deleted = 0")
    long countUsageInTimeRange(@Param("userIdentifier") String userIdentifier,
                              @Param("toolName") String toolName,
                              @Param("startTime") LocalDateTime startTime,
                              @Param("endTime") LocalDateTime endTime);

    /**
     * 查询指定时间范围内的使用记录
     */
    @Select("SELECT * FROM tool_usage_records WHERE user_identifier = #{userIdentifier} " +
            "AND tool_name = #{toolName} AND usage_time >= #{startTime} AND usage_time <= #{endTime} " +
            "AND deleted = 0 ORDER BY usage_time DESC")
    List<ToolUsageRecord> selectUsageInTimeRange(@Param("userIdentifier") String userIdentifier,
                                                @Param("toolName") String toolName,
                                                @Param("startTime") LocalDateTime startTime,
                                                @Param("endTime") LocalDateTime endTime);

    /**
     * 统计工具的总使用次数
     */
    @Select("SELECT COUNT(*) FROM tool_usage_records WHERE tool_name = #{toolName} " +
            "AND usage_time >= #{startTime} AND deleted = 0")
    long countToolUsage(@Param("toolName") String toolName, @Param("startTime") LocalDateTime startTime);

    /**
     * 统计用户的总使用次数
     */
    @Select("SELECT COUNT(*) FROM tool_usage_records WHERE user_identifier = #{userIdentifier} " +
            "AND usage_time >= #{startTime} AND deleted = 0")
    long countUserUsage(@Param("userIdentifier") String userIdentifier, @Param("startTime") LocalDateTime startTime);

    /**
     * 删除指定时间之前的记录（清理历史数据）
     */
    @Select("UPDATE tool_usage_records SET deleted = 1 WHERE usage_time < #{cutoffTime}")
    int deleteOldRecords(@Param("cutoffTime") LocalDateTime cutoffTime);
}