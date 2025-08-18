package com.flyerme.weautotools.util

import org.redisson.api.RLock
import org.redisson.api.RedissonClient
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit
import java.util.function.Supplier

/**
 * 分布式锁工具类 (Kotlin版本)
 * 基于Redisson实现的分布式锁操作工具
 * 
 * 提供与Java版本完全兼容的API，同时增加Kotlin特性
 *
 * @author WeAutoTools Team
 * @version 2.0.0
 * @since 2025-08-18
 */
@Component
open class DistributedLockUtil {

    @Autowired
    private lateinit var redissonClient: RedissonClient

    companion object {
        private val logger = LoggerFactory.getLogger(DistributedLockUtil::class.java)
        
        /**
         * 默认锁等待时间（秒）
         */
        const val DEFAULT_WAIT_TIME = 10L

        /**
         * 默认锁持有时间（秒）
         */
        const val DEFAULT_LEASE_TIME = 30L

        /**
         * 生成锁的key
         * 兼容Java版本的静态方法
         */
        @JvmStatic
        fun generateLockKey(vararg parts: String): String {
            return "lock:${parts.joinToString(":")}"
        }
    }

    /**
     * 获取分布式锁并执行业务逻辑
     * 兼容Java版本的API
     *
     * @param lockKey 锁的key
     * @param supplier 业务逻辑
     * @return 业务逻辑执行结果
     */
    fun <T> executeWithLock(lockKey: String, supplier: Supplier<T>): T {
        return executeWithLock(lockKey, DEFAULT_WAIT_TIME, DEFAULT_LEASE_TIME, TimeUnit.SECONDS, supplier)
    }

    /**
     * 获取分布式锁并执行业务逻辑
     * 兼容Java版本的API
     *
     * @param lockKey 锁的key
     * @param waitTime 等待时间
     * @param leaseTime 锁持有时间
     * @param timeUnit 时间单位
     * @param supplier 业务逻辑
     * @return 业务逻辑执行结果
     */
    fun <T> executeWithLock(
        lockKey: String,
        waitTime: Long,
        leaseTime: Long,
        timeUnit: TimeUnit,
        supplier: Supplier<T>
    ): T {
        val lock = redissonClient.getLock(lockKey)
        return try {
            val acquired = lock.tryLock(waitTime, leaseTime, timeUnit)
            if (acquired) {
                logger.debug("成功获取分布式锁: {}", lockKey)
                supplier.get()
            } else {
                logger.warn("获取分布式锁失败: {}", lockKey)
                throw RuntimeException("获取分布式锁失败，请稍后重试")
            }
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            logger.error("获取分布式锁被中断: {}", lockKey, e)
            throw RuntimeException("获取分布式锁被中断", e)
        } finally {
            if (lock.isHeldByCurrentThread) {
                lock.unlock()
                logger.debug("释放分布式锁: {}", lockKey)
            }
        }
    }

    /**
     * 获取分布式锁并执行业务逻辑（无返回值）
     * 兼容Java版本的API
     *
     * @param lockKey 锁的key
     * @param runnable 业务逻辑
     */
    fun executeWithLock(lockKey: String, runnable: Runnable) {
        executeWithLock(lockKey, DEFAULT_WAIT_TIME, DEFAULT_LEASE_TIME, TimeUnit.SECONDS, runnable)
    }

    /**
     * 获取分布式锁并执行业务逻辑（无返回值）
     * 兼容Java版本的API
     *
     * @param lockKey 锁的key
     * @param waitTime 等待时间
     * @param leaseTime 锁持有时间
     * @param timeUnit 时间单位
     * @param runnable 业务逻辑
     */
    fun executeWithLock(
        lockKey: String,
        waitTime: Long,
        leaseTime: Long,
        timeUnit: TimeUnit,
        runnable: Runnable
    ) {
        executeWithLock(lockKey, waitTime, leaseTime, timeUnit) {
            runnable.run()
            @Suppress("UNUSED_EXPRESSION")
            null
        }
    }

    /**
     * 尝试获取锁
     * 兼容Java版本的API
     *
     * @param lockKey 锁的key
     * @return 锁对象，需要手动释放
     */
    fun tryLock(lockKey: String): RLock? {
        return tryLock(lockKey, DEFAULT_WAIT_TIME, DEFAULT_LEASE_TIME, TimeUnit.SECONDS)
    }

    /**
     * 尝试获取锁
     * 兼容Java版本的API
     *
     * @param lockKey 锁的key
     * @param waitTime 等待时间
     * @param leaseTime 锁持有时间
     * @param timeUnit 时间单位
     * @return 锁对象，需要手动释放
     */
    fun tryLock(lockKey: String, waitTime: Long, leaseTime: Long, timeUnit: TimeUnit): RLock? {
        val lock = redissonClient.getLock(lockKey)
        return try {
            val acquired = lock.tryLock(waitTime, leaseTime, timeUnit)
            if (acquired) {
                logger.debug("成功获取分布式锁: {}", lockKey)
                lock
            } else {
                logger.warn("获取分布式锁失败: {}", lockKey)
                null
            }
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            logger.error("获取分布式锁被中断: {}", lockKey, e)
            throw RuntimeException("获取分布式锁被中断", e)
        }
    }

    /**
     * Kotlin风格的扩展方法
     * 使用lambda表达式执行带锁的操作
     */
    fun <T> withLock(
        lockKey: String,
        waitTime: Long = DEFAULT_WAIT_TIME,
        leaseTime: Long = DEFAULT_LEASE_TIME,
        timeUnit: TimeUnit = TimeUnit.SECONDS,
        action: () -> T
    ): T {
        return executeWithLock(lockKey, waitTime, leaseTime, timeUnit, Supplier { action() })
    }

    /**
     * Kotlin风格的扩展方法（无返回值）
     */
    fun withLock(
        lockKey: String,
        waitTime: Long = DEFAULT_WAIT_TIME,
        leaseTime: Long = DEFAULT_LEASE_TIME,
        timeUnit: TimeUnit = TimeUnit.SECONDS,
        action: () -> Unit
    ) {
        executeWithLock(lockKey, waitTime, leaseTime, timeUnit, Runnable { action() })
    }
}
