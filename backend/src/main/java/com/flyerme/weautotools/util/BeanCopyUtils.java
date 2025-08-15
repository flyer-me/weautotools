package com.flyerme.weautotools.util;

import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Bean拷贝工具类
 *
 * @author WeAutoTools Team
 * @version 1.0.0
 * @since 2025-08-15
 */
public class BeanCopyUtils {

    /**
     * 拷贝对象属性
     *
     * @param source 源对象
     * @param targetClass 目标类型
     * @param <T> 目标类型
     * @return 目标对象
     */
    public static <T> T copyProperties(Object source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }
        
        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(source, target);
            return target;
        } catch (Exception e) {
            throw new RuntimeException("Bean拷贝失败", e);
        }
    }

    /**
     * 拷贝对象列表
     *
     * @param sourceList 源对象列表
     * @param targetClass 目标类型
     * @param <T> 目标类型
     * @return 目标对象列表
     */
    public static <T> List<T> copyPropertiesList(List<?> sourceList, Class<T> targetClass) {
        if (sourceList == null || sourceList.isEmpty()) {
            return List.of();
        }
        
        return sourceList.stream()
                .map(source -> copyProperties(source, targetClass))
                .collect(Collectors.toList());
    }

    /**
     * 拷贝对象属性（忽略指定属性）
     *
     * @param source 源对象
     * @param target 目标对象
     * @param ignoreProperties 忽略的属性名
     */
    public static void copyProperties(Object source, Object target, String... ignoreProperties) {
        if (source == null || target == null) {
            return;
        }
        BeanUtils.copyProperties(source, target, ignoreProperties);
    }
}
