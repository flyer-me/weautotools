package com.flyerme.weautotools.service.impl;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.flyerme.weautotools.common.BaseEntity;
import com.flyerme.weautotools.common.BusinessException;
import com.flyerme.weautotools.mapper.BaseConverter;
import com.flyerme.weautotools.service.BaseService;

import java.time.LocalDateTime;
import java.util.List;

public class BaseServiceImpl<T extends BaseEntity, R, S>
        extends ServiceImpl<BaseMapper<T>, T>
        implements BaseService<T, R, S> {

    protected final BaseConverter<T, R, S> converter;

    protected BaseServiceImpl(BaseConverter<T, R, S> converter) {
        this.converter = converter;
    }

    public S create(R request) {
        // 验证请求参数,业务规则
        validateCreateRequest(request);
        checkCreateBusinessRules(request);

        // 创建实体
        T entity = createEntityFromRequest(request);
        setCreateDefaults(entity);

        if (!save(entity)) {
            throw new BusinessException("通过" + request.getClass() + "创建失败");
        }

        return converter.toResponse(entity);
    }

    /**
     * 根据ID获取实体的通用方法
     *
     * @param id 实体ID
     * @return 响应DTO
     */
    public S getByIdSerializable(Long id) {
        T entity = getById(id);
        if (entity == null) {
            return null;
        }
        return converter.toResponse(entity);
    }

    /**
     * 更新实体的通用模板方法
     *
     * @param id 实体ID
     * @param request 请求DTO
     * @return 响应DTO
     */
    public S update(Long id, R request) {
        T entity = getById(id);
        if (entity == null) {
            throw new BusinessException("update的实体不存在: " + id);
        }

        validateUpdateRequest(id, request);
        checkUpdateBusinessRules(id, request, entity);

        // 更新实体属性
        converter.updateEntityFromRequest(entity, request);
        setUpdateDefaults(entity);

        if (!updateById(entity)) {
            throw new BusinessException("更新" + entity.getClass() + "失败");
        }

        return converter.toResponse(entity);
    }

    /**
     * 删除实体的通用方法
     *
     * @param id 实体ID
     */
    public void delete(Long id) {
        if (!removeById(id)) {
            throw new BusinessException("删除失败");
        }

    }

    /**
     * 获取所有实体的通用方法
     *
     * @return 响应DTO列表
     */
    public List<S> getAll() {
        List<T> entities = list();
        return entities.stream()
                .map(converter::toResponse).toList();
    }

    /**
     * 从请求DTO创建实体
     */
    T createEntityFromRequest(R request) {
        return converter.toEntity(request);
    }

    /**
     * 设置创建时的默认值
     */
    void setCreateDefaults(T entity) {
        LocalDateTime now = LocalDateTime.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setDeleted(0);
        entity.setVersion(0);
    }

    /**
     * 设置更新时的默认值
     */
    void setUpdateDefaults(T entity) {
        entity.setUpdatedAt(LocalDateTime.now());
    }

    protected void validateCreateRequest(R request) {}
    protected void validateUpdateRequest(Long id, R request){}
    protected void checkCreateBusinessRules(R request){}
    protected void checkUpdateBusinessRules(Long id, R request, T entity){}
    protected void checkDeleteBusinessRules(Long id, T entity){}
}
