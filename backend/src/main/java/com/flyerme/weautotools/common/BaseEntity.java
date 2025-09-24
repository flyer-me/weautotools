package com.flyerme.weautotools.common;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * 基础实体类
 * 包含通用字段：id、创建时间、更新时间、逻辑删除标记
 *
 * @author WeAutoTools Team
 * @version 1.0.0
 * @since 2025-08-15
 */
@Data
public abstract class BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_UUID)
    protected String id;

    private Instant createdAt;

    private Instant updatedAt;

    /**
     * 逻辑删除标记
     * 0: 未删除, 1: 已删除
     */
    @TableLogic
    private Integer deleted = 0;

    @Version
    private Integer version = 0;
}