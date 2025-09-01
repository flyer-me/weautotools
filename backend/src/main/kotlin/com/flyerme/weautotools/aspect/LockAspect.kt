package com.flyerme.weautotools.aspect

import com.flyerme.weautotools.annotation.Lock
import com.flyerme.weautotools.exception.BusinessException
import com.flyerme.weautotools.util.LockKeyGenerator
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.redisson.api.RedissonClient
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * 分布式锁AOP切面 (Kotlin版本)
 * 处理@Lock注解的方法
 *
 * @author WeAutoTools Team
 * @version 1.0.0
 * @since 2025-08-17
 */
@Aspect
@Component
open class LockAspect {

    @Autowired
    internal lateinit var redissonClient: RedissonClient

    @Autowired
    internal lateinit var lockKeyGenerator: LockKeyGenerator

    companion object {
        private val logger = LoggerFactory.getLogger(LockAspect::class.java)
    }

    @Around("@annotation(lock)")
    @Throws(Throwable::class)
    fun around(joinPoint: ProceedingJoinPoint, lock: Lock): Any? {
        val lockKey = lockKeyGenerator.generateKey(joinPoint, lock)
        val redisLock = redissonClient.getLock(lockKey)

        var acquired = false
        return try {
            // 尝试获取锁
            acquired = redisLock.tryLock(
                lock.waitTime,
                lock.leaseTime,
                lock.timeUnit
            )

            if (!acquired) {
                logger.warn("获取分布式锁失败: {}", lockKey)
                throw BusinessException(lock.failMessage)
            }

            logger.debug("成功获取分布式锁: {}", lockKey)
            
            // 执行目标方法
            joinPoint.proceed()

        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            logger.error("获取分布式锁被中断: {}", lockKey, e)
            throw BusinessException("获取分布式锁被中断")
        } finally {
            // 自动释放锁
            if (acquired && redisLock.isHeldByCurrentThread) {
                redisLock.unlock()
                logger.debug("释放分布式锁: {}", lockKey)
            }
        }
    }
}
