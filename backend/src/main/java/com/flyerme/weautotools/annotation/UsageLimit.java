package com.flyerme.weautotools.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 使用限制注解
 * 用于标记需要进行使用次数限制检查的工具方法
 *
 * @author WeAutoTools Team
 * @version 1.0.2
 * @since 2025-09-12
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UsageLimit {
    
    /**
     * 工具名称
     * @return 工具名称
     */
    String toolName();
    
    /**
     * 工具类型
     * @return 工具类型，默认为DEFAULT
     */
    String toolType() default "DEFAULT";
    
    /**
     * 是否需要认证
     * @return 是否需要认证，默认false
     */
    boolean requireAuth() default false;
    
    /**
     * 自定义错误信息
     * @return 错误信息
     */
    String message() default "使用次数已达限制";
}