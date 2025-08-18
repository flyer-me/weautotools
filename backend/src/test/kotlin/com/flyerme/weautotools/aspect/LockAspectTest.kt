package com.flyerme.weautotools.aspect

import com.flyerme.weautotools.annotation.Lock
import com.flyerme.weautotools.exception.BusinessException
import com.flyerme.weautotools.util.LockKeyGenerator
import org.aspectj.lang.ProceedingJoinPoint
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.redisson.api.RLock
import org.redisson.api.RedissonClient
import java.util.concurrent.TimeUnit

/**
 * LockAspect测试类 (Kotlin版本)
 *
 * @author WeAutoTools Team
 * @version 1.0.0
 * @since 2025-08-17
 */
@ExtendWith(MockitoExtension::class)
class LockAspectTest {

    @Mock
    private lateinit var redissonClient: RedissonClient

    @Mock
    private lateinit var lockKeyGenerator: LockKeyGenerator

    @Mock
    private lateinit var rLock: RLock

    @Mock
    private lateinit var joinPoint: ProceedingJoinPoint

    private lateinit var lockAspect: LockAspect

    @BeforeEach
    fun setUp() {
        lockAspect = LockAspect().apply {
            redissonClient = this@LockAspectTest.redissonClient
            lockKeyGenerator = this@LockAspectTest.lockKeyGenerator
        }
    }

    @Test
    fun `should execute method successfully when lock acquired`() {
        // Given
        val lockKey = "test:lock:key"
        val expectedResult = "success"
        val lock = createLockAnnotation("test-key")

        `when`(lockKeyGenerator.generateKey(joinPoint, lock)).thenReturn(lockKey)
        `when`(redissonClient.getLock(lockKey)).thenReturn(rLock)
        `when`(rLock.tryLock(10L, 30L, TimeUnit.SECONDS)).thenReturn(true)
        `when`(rLock.isHeldByCurrentThread).thenReturn(true)
        `when`(joinPoint.proceed()).thenReturn(expectedResult)

        // When
        val result = lockAspect.around(joinPoint, lock)

        // Then
        assertEquals(expectedResult, result)
        verify(rLock).tryLock(10L, 30L, TimeUnit.SECONDS)
        verify(joinPoint).proceed()
        verify(rLock).unlock()
    }

    @Test
    fun `should throw BusinessException when lock acquisition fails`() {
        // Given
        val lockKey = "test:lock:key"
        val lock = createLockAnnotation("test-key", failMessage = "自定义锁失败消息")

        `when`(lockKeyGenerator.generateKey(joinPoint, lock)).thenReturn(lockKey)
        `when`(redissonClient.getLock(lockKey)).thenReturn(rLock)
        `when`(rLock.tryLock(10L, 30L, TimeUnit.SECONDS)).thenReturn(false)

        // When & Then
        val exception = assertThrows(BusinessException::class.java) {
            lockAspect.around(joinPoint, lock)
        }

        assertEquals("自定义锁失败消息", exception.message)
        verify(joinPoint, never()).proceed()
        verify(rLock, never()).unlock()
    }

    @Test
    fun `should handle InterruptedException properly`() {
        // Given
        val lockKey = "test:lock:key"
        val lock = createLockAnnotation("test-key")

        `when`(lockKeyGenerator.generateKey(joinPoint, lock)).thenReturn(lockKey)
        `when`(redissonClient.getLock(lockKey)).thenReturn(rLock)
        `when`(rLock.tryLock(10L, 30L, TimeUnit.SECONDS))
            .thenThrow(InterruptedException("Test interruption"))

        // When & Then
        val exception = assertThrows(BusinessException::class.java) {
            lockAspect.around(joinPoint, lock)
        }

        assertEquals("获取分布式锁被中断", exception.message)
        assertTrue(Thread.currentThread().isInterrupted)
    }

    @Test
    fun `should not unlock when autoRelease is false`() {
        // Given
        val lockKey = "test:lock:key"
        val expectedResult = "success"
        val lock = createLockAnnotation("test-key", autoRelease = false)

        `when`(lockKeyGenerator.generateKey(joinPoint, lock)).thenReturn(lockKey)
        `when`(redissonClient.getLock(lockKey)).thenReturn(rLock)
        `when`(rLock.tryLock(10L, 30L, TimeUnit.SECONDS)).thenReturn(true)
        `when`(joinPoint.proceed()).thenReturn(expectedResult)

        // When
        val result = lockAspect.around(joinPoint, lock)

        // Then
        assertEquals(expectedResult, result)
        verify(rLock, never()).unlock()
    }

    @Test
    fun `should not unlock when lock is not held by current thread`() {
        // Given
        val lockKey = "test:lock:key"
        val expectedResult = "success"
        val lock = createLockAnnotation("test-key")

        `when`(lockKeyGenerator.generateKey(joinPoint, lock)).thenReturn(lockKey)
        `when`(redissonClient.getLock(lockKey)).thenReturn(rLock)
        `when`(rLock.tryLock(10L, 30L, TimeUnit.SECONDS)).thenReturn(true)
        `when`(rLock.isHeldByCurrentThread).thenReturn(false)
        `when`(joinPoint.proceed()).thenReturn(expectedResult)

        // When
        val result = lockAspect.around(joinPoint, lock)

        // Then
        assertEquals(expectedResult, result)
        verify(rLock, never()).unlock()
    }

    @Test
    fun `should use custom timeout values`() {
        // Given
        val lockKey = "test:lock:key"
        val expectedResult = "success"
        val lock = createLockAnnotation(
            key = "test-key",
            waitTime = 5L,
            leaseTime = 15L,
            timeUnit = TimeUnit.MINUTES
        )

        `when`(lockKeyGenerator.generateKey(joinPoint, lock)).thenReturn(lockKey)
        `when`(redissonClient.getLock(lockKey)).thenReturn(rLock)
        `when`(rLock.tryLock(5L, 15L, TimeUnit.MINUTES)).thenReturn(true)
        `when`(rLock.isHeldByCurrentThread).thenReturn(true)
        `when`(joinPoint.proceed()).thenReturn(expectedResult)

        // When
        val result = lockAspect.around(joinPoint, lock)

        // Then
        assertEquals(expectedResult, result)
        verify(rLock).tryLock(5L, 15L, TimeUnit.MINUTES)
    }

    @Test
    fun `should unlock even when method throws exception`() {
        // Given
        val lockKey = "test:lock:key"
        val lock = createLockAnnotation("test-key")
        val methodException = RuntimeException("Method failed")

        `when`(lockKeyGenerator.generateKey(joinPoint, lock)).thenReturn(lockKey)
        `when`(redissonClient.getLock(lockKey)).thenReturn(rLock)
        `when`(rLock.tryLock(10L, 30L, TimeUnit.SECONDS)).thenReturn(true)
        `when`(rLock.isHeldByCurrentThread).thenReturn(true)
        `when`(joinPoint.proceed()).thenThrow(methodException)

        // When & Then
        val exception = assertThrows(RuntimeException::class.java) {
            lockAspect.around(joinPoint, lock)
        }

        assertEquals("Method failed", exception.message)
        verify(rLock).unlock() // 确保即使方法抛异常也会释放锁
    }

    // 辅助方法：创建Lock注解实例
    private fun createLockAnnotation(
        key: String,
        prefix: String = "lock",
        waitTime: Long = 10L,
        leaseTime: Long = 30L,
        timeUnit: TimeUnit = TimeUnit.SECONDS,
        failMessage: String = "获取分布式锁失败，请稍后重试",
        autoRelease: Boolean = true
    ): Lock {
        return object : Lock {
            override fun annotationClass() = Lock::class
            override val key = key
            override val prefix = prefix
            override val waitTime = waitTime
            override val leaseTime = leaseTime
            override val timeUnit = timeUnit
            override val failMessage = failMessage
            override val autoRelease = autoRelease
        }
    }
}
