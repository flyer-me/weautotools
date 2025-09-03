package com.flyerme.weautotools.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.flyerme.weautotools.entity.ClickCounter;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

/**
 * 点击计数器Mapper接口
 * 只保留业务特定的查询方法，通用CRUD由BaseService处理
 *
 * @author WeAutoTools Team
 * @version 1.0.0
 * @since 2025-08-15
 */
@Mapper
public interface ClickCounterMapper extends BaseMapper<ClickCounter> {
    /**
     * 根据名称查询计数器
     */
    @Select("SELECT * FROM click_counter WHERE counter_name = #{counterName} AND deleted = 0")
    ClickCounter selectByName(String counterName);

    /**
     * 增加点击次数
     */
    @Update("UPDATE click_counter SET click_count = click_count + 1, last_click_time = #{lastClickTime}, " +
            "updated_at = #{updatedAt}, version = version + 1 " +
            "WHERE id = #{id} AND enabled = true AND deleted = 0")
    int incrementClickCount(@Param("id") Long id,
                           @Param("lastClickTime") LocalDateTime lastClickTime,
                           @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * 根据名称增加点击次数
     */
    @Update("UPDATE click_counter SET click_count = click_count + 1, last_click_time = #{lastClickTime}, " +
            "updated_at = #{updatedAt}, version = version + 1 " +
            "WHERE counter_name = #{counterName} AND enabled = true AND deleted = 0")
    int incrementClickCountByName(@Param("counterName") String counterName,
                                 @Param("lastClickTime") LocalDateTime lastClickTime,
                                 @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * 检查计数器名称是否存在（排除指定ID）
     */
    @Select("SELECT COUNT(*) FROM click_counter WHERE counter_name = #{counterName} AND id != #{excludeId} AND deleted = 0")
    long countByNameExcludeId(@Param("counterName") String counterName, @Param("excludeId") Long excludeId);
}
