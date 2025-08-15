package com.flyerme.weautotools.repository;

import com.flyerme.weautotools.entity.ClickCounter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 点击计数器Repository接口
 *
 * @author WeAutoTools Team
 * @version 1.0.0
 * @since 2025-08-15
 */
@Repository
public interface ClickCounterRepository extends JpaRepository<ClickCounter, Long> {

    /**
     * 根据计数器名称查询（未删除）
     */
    @Query("SELECT c FROM ClickCounter c WHERE c.counterName = :counterName AND c.deleted = 0")
    Optional<ClickCounter> findByCounterNameAndDeletedFalse(@Param("counterName") String counterName);

    /**
     * 查询所有未删除的计数器
     */
    @Query("SELECT c FROM ClickCounter c WHERE c.deleted = 0 ORDER BY c.createdAt DESC")
    List<ClickCounter> findAllNotDeleted();

    /**
     * 查询所有启用且未删除的计数器
     */
    @Query("SELECT c FROM ClickCounter c WHERE c.enabled = true AND c.deleted = 0 ORDER BY c.createdAt DESC")
    List<ClickCounter> findAllEnabledAndNotDeleted();

    /**
     * 增加点击次数
     */
    @Modifying
    @Query("UPDATE ClickCounter c SET c.clickCount = c.clickCount + 1, c.lastClickTime = :lastClickTime, " +
           "c.updatedAt = :updatedAt WHERE c.id = :id AND c.enabled = true AND c.deleted = 0")
    int incrementClickCount(@Param("id") Long id, 
                           @Param("lastClickTime") LocalDateTime lastClickTime,
                           @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * 根据名称增加点击次数
     */
    @Modifying
    @Query("UPDATE ClickCounter c SET c.clickCount = c.clickCount + 1, c.lastClickTime = :lastClickTime, " +
           "c.updatedAt = :updatedAt WHERE c.counterName = :counterName AND c.enabled = true AND c.deleted = 0")
    int incrementClickCountByName(@Param("counterName") String counterName,
                                 @Param("lastClickTime") LocalDateTime lastClickTime,
                                 @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * 统计未删除的计数器总数
     */
    @Query("SELECT COUNT(c) FROM ClickCounter c WHERE c.deleted = 0")
    long countNotDeleted();

    /**
     * 统计启用且未删除的计数器总数
     */
    @Query("SELECT COUNT(c) FROM ClickCounter c WHERE c.enabled = true AND c.deleted = 0")
    long countEnabledAndNotDeleted();
}
