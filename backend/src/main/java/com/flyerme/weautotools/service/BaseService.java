package com.flyerme.weautotools.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.flyerme.weautotools.common.BaseEntity;

import java.util.List;

/**
 * 基础Service
 * 提供通用的数据库操作和业务逻辑模板，如不需要业务逻辑模板，请直接继承IService以简化设计
 *
 * @param <T> 实体类型
 * @param <R> 请求DTO类型
 * @param <S> 响应DTO类型
 * @author WeAutoTools Team
 * @version 1.0.0
 * @since 2025-08-17
 */

public interface BaseService<T extends BaseEntity, R, S> extends IService<T> {
    S update(Long id, R request);
    List<S> getAll();
    S create(R request);
    S getByIdSerializable(Long id);
    void delete(Long id);
}
