package com.flyerme.weautotools.mapper;

import com.flyerme.weautotools.entity.ClickCounter;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 点击计数器Mapper接口
 * 只保留业务特定的查询方法，通用CRUD由BaseService处理
 *
 * @author WeAutoTools Team
 * @version 1.0.0
 * @since 2025-08-15
 */
@Mapper
public interface ClickCounterMapper {

    // ==================== 基础CRUD方法 (BaseService需要) ====================

    /**
     * 根据ID查询计数器
     */
    @Select("SELECT * FROM click_counter WHERE id = #{id} AND deleted = 0")
    ClickCounter selectById(Long id);

    /**
     * 查询所有计数器
     */
    @Select("SELECT * FROM click_counter WHERE deleted = 0 ORDER BY created_at DESC")
    List<ClickCounter> selectAll();

    /**
     * 插入计数器
     */
    @Insert("INSERT INTO click_counter (counter_name, description, click_count, enabled, created_at, updated_at, deleted, version) " +
            "VALUES (#{counterName}, #{description}, #{clickCount}, #{enabled}, #{createdAt}, #{updatedAt}, #{deleted}, #{version})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ClickCounter clickCounter);

    /**
     * 根据ID更新计数器
     */
    @Update("UPDATE click_counter SET counter_name = #{counterName}, description = #{description}, " +
            "enabled = #{enabled}, updated_at = #{updatedAt}, version = version + 1 " +
            "WHERE id = #{id} AND deleted = 0 AND version = #{version}")
    int updateById(ClickCounter clickCounter);

    /**
     * 逻辑删除计数器
     */
    @Update("UPDATE click_counter SET deleted = 1, updated_at = #{updatedAt} WHERE id = #{id} AND deleted = 0")
    int deleteById(@Param("id") Long id, @Param("updatedAt") LocalDateTime updatedAt);

    // ==================== 业务特定查询方法 ====================

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
