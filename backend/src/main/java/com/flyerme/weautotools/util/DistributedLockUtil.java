package com.flyerme.weautotools.util;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 分布式锁工具类
 * 基于Redisson实现的分布式锁操作工具
 *
 * @author WeAutoTools Team
 * @version 1.0.0
 * @since 2025-08-17
 */
@Slf4j
@Component
public class DistributedLockUtil {

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 默认锁等待时间（秒）
     */
    private static final long DEFAULT_WAIT_TIME = 10L;

    /**
     * 默认锁持有时间（秒）
     */
    private static final long DEFAULT_LEASE_TIME = 30L;

    /**
     * 获取分布式锁并执行业务逻辑
     *
     * @param lockKey 锁的key
     * @param supplier 业务逻辑
     * @param <T> 返回值类型
     * @return 业务逻辑执行结果
     */
    public <T> T executeWithLock(String lockKey, Supplier<T> supplier) {
        return executeWithLock(lockKey, DEFAULT_WAIT_TIME, DEFAULT_LEASE_TIME, TimeUnit.SECONDS, supplier);
    }

    /**
     * 获取分布式锁并执行业务逻辑
     *
     * @param lockKey 锁的key
     * @param waitTime 等待时间
     * @param leaseTime 锁持有时间
     * @param timeUnit 时间单位
     * @param supplier 业务逻辑
     * @param <T> 返回值类型
     * @return 业务逻辑执行结果
     */
    public <T> T executeWithLock(String lockKey, long waitTime, long leaseTime, 
                                TimeUnit timeUnit, Supplier<T> supplier) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            boolean acquired = lock.tryLock(waitTime, leaseTime, timeUnit);
            if (acquired) {
                log.debug("成功获取分布式锁: {}", lockKey);
                return supplier.get();
            } else {
                log.warn("获取分布式锁失败: {}", lockKey);
                throw new RuntimeException("获取分布式锁失败，请稍后重试");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取分布式锁被中断: {}", lockKey, e);
            throw new RuntimeException("获取分布式锁被中断", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("释放分布式锁: {}", lockKey);
            }
        }
    }

    /**
     * 获取分布式锁并执行业务逻辑（无返回值）
     *
     * @param lockKey 锁的key
     * @param runnable 业务逻辑
     */
    public void executeWithLock(String lockKey, Runnable runnable) {
        executeWithLock(lockKey, DEFAULT_WAIT_TIME, DEFAULT_LEASE_TIME, TimeUnit.SECONDS, runnable);
    }

    /**
     * 获取分布式锁并执行业务逻辑（无返回值）
     *
     * @param lockKey 锁的key
     * @param waitTime 等待时间
     * @param leaseTime 锁持有时间
     * @param timeUnit 时间单位
     * @param runnable 业务逻辑
     */
    public void executeWithLock(String lockKey, long waitTime, long leaseTime, 
                               TimeUnit timeUnit, Runnable runnable) {
        executeWithLock(lockKey, waitTime, leaseTime, timeUnit, () -> {
            runnable.run();
            return null;
        });
    }

    /**
     * 尝试获取锁
     *
     * @param lockKey 锁的key
     * @return 锁对象，需要手动释放
     */
    public RLock tryLock(String lockKey) {
        return tryLock(lockKey, DEFAULT_WAIT_TIME, DEFAULT_LEASE_TIME, TimeUnit.SECONDS);
    }

    /**
     * 尝试获取锁
     *
     * @param lockKey 锁的key
     * @param waitTime 等待时间
     * @param leaseTime 锁持有时间
     * @param timeUnit 时间单位
     * @return 锁对象，需要手动释放
     */
    public RLock tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit timeUnit) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            boolean acquired = lock.tryLock(waitTime, leaseTime, timeUnit);
            if (acquired) {
                log.debug("成功获取分布式锁: {}", lockKey);
                return lock;
            } else {
                log.warn("获取分布式锁失败: {}", lockKey);
                return null;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取分布式锁被中断: {}", lockKey, e);
            throw new RuntimeException("获取分布式锁被中断", e);
        }
    }

    /**
     * 释放锁
     *
     * @param lock 锁对象
     */
    public void unlock(RLock lock) {
        if (lock != null && lock.isHeldByCurrentThread()) {
            lock.unlock();
            log.debug("释放分布式锁成功");
        }
    }

    /**
     * 生成锁的key
     *
     * @param prefix 前缀
     * @param identifier 标识符
     * @return 锁的key
     */
    public static String generateLockKey(String prefix, String identifier) {
        return String.format("lock:%s:%s", prefix, identifier);
    }

    /**
     * 生成锁的key
     *
     * @param prefix 前缀
     * @param identifier 标识符
     * @param suffix 后缀
     * @return 锁的key
     */
    public static String generateLockKey(String prefix, String identifier, String suffix) {
        return String.format("lock:%s:%s:%s", prefix, identifier, suffix);
    }
}
