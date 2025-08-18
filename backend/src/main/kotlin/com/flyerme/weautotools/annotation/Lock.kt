package com.flyerme.weautotools.annotation

import java.util.concurrent.TimeUnit

/**
 * 分布式锁注解
 * 用于方法级别的分布式锁控制
 *
 * @author WeAutoTools Team
 * @version 1.0.0
 * @since 2025-08-17
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Lock(
    /**
     * 锁的key，支持SpEL表达式
     * 例如：'user:' + #userId 或者 'order:' + #order.id
     */
    val key: String,

    /**
     * 锁的前缀，默认为 "lock"
     */
    val prefix: String = "lock",

    /**
     * 等待锁的时间，默认10秒
     */
    val waitTime: Long = 10L,

    /**
     * 锁的持有时间，默认30秒
     */
    val leaseTime: Long = 30L,

    /**
     * 时间单位，默认为秒
     */
    val timeUnit: TimeUnit = TimeUnit.SECONDS,

    /**
     * 获取锁失败时的错误消息
     */
    val failMessage: String = "获取分布式锁失败，请稍后重试",

    /**
     * 是否自动释放锁，默认为true
     * 如果为false，需要手动释放锁
     */
    val autoRelease: Boolean = true
)
