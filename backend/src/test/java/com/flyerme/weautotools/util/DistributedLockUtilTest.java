package com.flyerme.weautotools.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 分布式锁工具类测试
 *
 * @author WeAutoTools Team
 * @version 1.0.0
 * @since 2025-08-17
 */
@ExtendWith(MockitoExtension.class)
class DistributedLockUtilTest {

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private RLock rLock;

    @InjectMocks
    private DistributedLockUtil distributedLockUtil;

    @BeforeEach
    void setUp() {
        // Setup will be done in individual test methods as needed
    }

    @Test
    void testExecuteWithLock_Success() throws InterruptedException {
        // Given
        String lockKey = "test:lock:key";
        String expectedResult = "success";
        when(redissonClient.getLock(lockKey)).thenReturn(rLock);
        when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);

        // When
        String result = distributedLockUtil.executeWithLock(lockKey, () -> expectedResult);

        // Then
        assertEquals(expectedResult, result);
        verify(rLock).tryLock(10L, 30L, TimeUnit.SECONDS);
        verify(rLock).unlock();
    }

    @Test
    void testExecuteWithLock_FailToAcquire() throws InterruptedException {
        // Given
        String lockKey = "test:lock:key";
        when(redissonClient.getLock(lockKey)).thenReturn(rLock);
        when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(false);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            distributedLockUtil.executeWithLock(lockKey, () -> "should not execute");
        });

        assertEquals("获取分布式锁失败，请稍后重试", exception.getMessage());
        verify(rLock, never()).unlock();
    }

    @Test
    void testExecuteWithLock_InterruptedException() throws InterruptedException {
        // Given
        String lockKey = "test:lock:key";
        when(redissonClient.getLock(lockKey)).thenReturn(rLock);
        when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class)))
                .thenThrow(new InterruptedException("Test interruption"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            distributedLockUtil.executeWithLock(lockKey, () -> "should not execute");
        });

        assertEquals("获取分布式锁被中断", exception.getMessage());
        assertTrue(Thread.currentThread().isInterrupted());
    }

    @Test
    void testExecuteWithLock_CustomTimeouts() throws InterruptedException {
        // Given
        String lockKey = "test:lock:key";
        long waitTime = 5L;
        long leaseTime = 15L;
        TimeUnit timeUnit = TimeUnit.MINUTES;
        String expectedResult = "custom timeout success";

        when(redissonClient.getLock(lockKey)).thenReturn(rLock);
        when(rLock.tryLock(waitTime, leaseTime, timeUnit)).thenReturn(true);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);

        // When
        String result = distributedLockUtil.executeWithLock(
                lockKey, waitTime, leaseTime, timeUnit, () -> expectedResult);

        // Then
        assertEquals(expectedResult, result);
        verify(rLock).tryLock(waitTime, leaseTime, timeUnit);
        verify(rLock).unlock();
    }

    @Test
    void testExecuteWithLock_Runnable() throws InterruptedException {
        // Given
        String lockKey = "test:lock:key";
        AtomicInteger counter = new AtomicInteger(0);
        when(redissonClient.getLock(lockKey)).thenReturn(rLock);
        when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);

        // When
        distributedLockUtil.executeWithLock(lockKey, counter::incrementAndGet);

        // Then
        assertEquals(1, counter.get());
        verify(rLock).unlock();
    }

    @Test
    void testTryLock_Success() throws InterruptedException {
        // Given
        String lockKey = "test:lock:key";
        when(redissonClient.getLock(lockKey)).thenReturn(rLock);
        when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);

        // When
        RLock result = distributedLockUtil.tryLock(lockKey);

        // Then
        assertNotNull(result);
        assertEquals(rLock, result);
        verify(rLock).tryLock(10L, 30L, TimeUnit.SECONDS);
    }

    @Test
    void testTryLock_Fail() throws InterruptedException {
        // Given
        String lockKey = "test:lock:key";
        when(redissonClient.getLock(lockKey)).thenReturn(rLock);
        when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(false);

        // When
        RLock result = distributedLockUtil.tryLock(lockKey);

        // Then
        assertNull(result);
    }

    @Test
    void testUnlock() {
        // Given
        when(rLock.isHeldByCurrentThread()).thenReturn(true);

        // When
        distributedLockUtil.unlock(rLock);

        // Then
        verify(rLock).unlock();
    }

    @Test
    void testUnlock_NotHeldByCurrentThread() {
        // Given
        when(rLock.isHeldByCurrentThread()).thenReturn(false);

        // When
        distributedLockUtil.unlock(rLock);

        // Then
        verify(rLock, never()).unlock();
    }

    @Test
    void testGenerateLockKey() {
        // Test two-parameter method
        String key1 = DistributedLockUtil.generateLockKey("user", "123");
        assertEquals("lock:user:123", key1);

        // Test three-parameter method
        String key2 = DistributedLockUtil.generateLockKey("order", "456", "payment");
        assertEquals("lock:order:456:payment", key2);
    }
}
