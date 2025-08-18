package com.flyerme.weautotools.service;

import com.flyerme.weautotools.common.BaseEntity;
import com.flyerme.weautotools.exception.BusinessException;
import com.flyerme.weautotools.util.BeanCopyUtils;
import com.flyerme.weautotools.util.DistributedLockUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Supplier;

/**
 * 基础Service抽象类
 * 提供通用的CRUD操作和业务逻辑模板
 *
 * @param <T> 实体类型
 * @param <R> 请求DTO类型
 * @param <S> 响应DTO类型
 * @author WeAutoTools Team
 * @version 1.0.0
 * @since 2025-08-17
 */
@Slf4j
public abstract class BaseService<T extends BaseEntity, R, S> {

    @Autowired
    protected DistributedLockUtil distributedLockUtil;

    /**
     * 获取实体类型
     */
    protected abstract Class<T> getEntityClass();

    /**
     * 获取响应DTO类型
     */
    protected abstract Class<S> getResponseClass();

    /**
     * 获取实体名称（用于日志和锁key）
     */
    protected abstract String getEntityName();

    /**
     * 根据ID查询实体
     */
    protected abstract T selectById(Long id);

    /**
     * 插入实体
     */
    protected abstract int insert(T entity);

    /**
     * 根据ID更新实体
     */
    protected abstract int updateById(T entity);

    /**
     * 删除实体（逻辑删除）
     */
    protected abstract int deleteById(Long id, LocalDateTime deleteTime);

    /**
     * 查询所有实体
     */
    protected abstract List<T> selectAll();

    /**
     * 创建实体的通用模板方法
     *
     * @param request 请求DTO
     * @return 响应DTO
     */
    @Transactional
    public S create(R request) {
        String lockKey = DistributedLockUtil.generateLockKey(getEntityName(), "create");
        
        return distributedLockUtil.executeWithLock(lockKey, () -> {
            // 验证请求参数
            validateCreateRequest(request);
            
            // 检查业务规则
            checkCreateBusinessRules(request);
            
            // 创建实体
            T entity = createEntityFromRequest(request);
            setCreateDefaults(entity);
            
            int result = insert(entity);
            if (result <= 0) {
                throw new BusinessException("创建" + getEntityName() + "失败");
            }
            
            log.info("创建{}成功: {}", getEntityName(), entity.getId());
            return convertToResponse(entity);
        });
    }

    /**
     * 根据ID获取实体的通用方法
     *
     * @param id 实体ID
     * @return 响应DTO
     */
    public S getById(Long id) {
        T entity = selectById(id);
        if (entity == null) {
            throw new BusinessException(getEntityName() + "不存在: " + id);
        }
        return convertToResponse(entity);
    }

    /**
     * 更新实体的通用模板方法
     *
     * @param id 实体ID
     * @param request 请求DTO
     * @return 响应DTO
     */
    @Transactional
    public S update(Long id, R request) {
        String lockKey = DistributedLockUtil.generateLockKey(getEntityName(), "update", String.valueOf(id));
        
        return distributedLockUtil.executeWithLock(lockKey, () -> {
            T entity = selectById(id);
            if (entity == null) {
                throw new BusinessException(getEntityName() + "不存在: " + id);
            }
            
            // 验证请求参数
            validateUpdateRequest(id, request);
            
            // 检查业务规则
            checkUpdateBusinessRules(id, request, entity);
            
            // 更新实体属性
            updateEntityFromRequest(entity, request);
            setUpdateDefaults(entity);
            
            int result = updateById(entity);
            if (result <= 0) {
                throw new BusinessException("更新" + getEntityName() + "失败");
            }
            
            log.info("更新{}成功: {}", getEntityName(), id);
            return convertToResponse(entity);
        });
    }

    /**
     * 删除实体的通用方法
     *
     * @param id 实体ID
     */
    @Transactional
    public void delete(Long id) {
        String lockKey = DistributedLockUtil.generateLockKey(getEntityName(), "delete", String.valueOf(id));
        
        distributedLockUtil.executeWithLock(lockKey, () -> {
            T entity = selectById(id);
            if (entity == null) {
                throw new BusinessException(getEntityName() + "不存在: " + id);
            }
            
            // 检查删除业务规则
            checkDeleteBusinessRules(id, entity);
            
            int result = deleteById(id, LocalDateTime.now());
            if (result <= 0) {
                throw new BusinessException("删除" + getEntityName() + "失败");
            }
            
            log.info("删除{}成功: {}", getEntityName(), id);
        });
    }

    /**
     * 获取所有实体的通用方法
     *
     * @return 响应DTO列表
     */
    public List<S> getAll() {
        List<T> entities = selectAll();
        return BeanCopyUtils.copyPropertiesList(entities, getResponseClass());
    }

    /**
     * 使用分布式锁执行业务逻辑
     *
     * @param lockKey 锁key
     * @param supplier 业务逻辑
     * @param <U> 返回值类型
     * @return 业务逻辑执行结果
     */
    protected <U> U executeWithLock(String lockKey, Supplier<U> supplier) {
        return distributedLockUtil.executeWithLock(lockKey, supplier);
    }

    /**
     * 转换为响应DTO
     */
    protected S convertToResponse(T entity) {
        return BeanCopyUtils.copyProperties(entity, getResponseClass());
    }

    /**
     * 从请求DTO创建实体
     */
    protected T createEntityFromRequest(R request) {
        return BeanCopyUtils.copyProperties(request, getEntityClass());
    }

    /**
     * 从请求DTO更新实体
     */
    protected void updateEntityFromRequest(T entity, R request) {
        BeanCopyUtils.copyProperties(request, entity, "id", "createdAt", "version");
    }

    /**
     * 设置创建时的默认值
     */
    protected void setCreateDefaults(T entity) {
        LocalDateTime now = LocalDateTime.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setDeleted(0);
        entity.setVersion(0);
    }

    /**
     * 设置更新时的默认值
     */
    protected void setUpdateDefaults(T entity) {
        entity.setUpdatedAt(LocalDateTime.now());
    }

    // 以下方法可以被子类重写以实现特定的业务逻辑

    /**
     * 验证创建请求参数
     */
    protected void validateCreateRequest(R request) {
        // 子类可以重写此方法进行特定验证
    }

    /**
     * 验证更新请求参数
     */
    protected void validateUpdateRequest(Long id, R request) {
        // 子类可以重写此方法进行特定验证
    }

    /**
     * 检查创建业务规则
     */
    protected void checkCreateBusinessRules(R request) {
        // 子类可以重写此方法进行特定业务规则检查
    }

    /**
     * 检查更新业务规则
     */
    protected void checkUpdateBusinessRules(Long id, R request, T entity) {
        // 子类可以重写此方法进行特定业务规则检查
    }

    /**
     * 检查删除业务规则
     */
    protected void checkDeleteBusinessRules(Long id, T entity) {
        // 子类可以重写此方法进行特定业务规则检查
    }
}
