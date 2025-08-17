package com.flyerme.weautotools.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁注解
 * 用于方法级别的分布式锁控制
 *
 * @author WeAutoTools Team
 * @version 1.0.0
 * @since 2025-08-17
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DistributedLock {

    /**
     * 锁的key，支持SpEL表达式
     * 例如：'user:' + #userId 或者 'order:' + #order.id
     */
    String key();

    /**
     * 锁的前缀，默认为 "lock"
     */
    String prefix() default "lock";

    /**
     * 等待锁的时间，默认10秒
     */
    long waitTime() default 10L;

    /**
     * 锁的持有时间，默认30秒
     */
    long leaseTime() default 30L;

    /**
     * 时间单位，默认为秒
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * 获取锁失败时的错误消息
     */
    String failMessage() default "获取分布式锁失败，请稍后重试";

    /**
     * 是否自动释放锁，默认为true
     * 如果为false，需要手动释放锁
     */
    boolean autoRelease() default true;
}
