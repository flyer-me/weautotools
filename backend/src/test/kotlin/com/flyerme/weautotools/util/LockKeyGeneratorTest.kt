package com.flyerme.weautotools.util

import com.flyerme.weautotools.annotation.Lock
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.reflect.MethodSignature
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import java.lang.reflect.Method

/**
 * LockKeyGenerator测试类 (Kotlin版本)
 *
 * @author WeAutoTools Team
 * @version 1.0.0
 * @since 2025-08-17
 */
@ExtendWith(MockitoExtension::class)
class LockKeyGeneratorTest {

    @Mock
    private lateinit var joinPoint: ProceedingJoinPoint

    @Mock
    private lateinit var methodSignature: MethodSignature

    private lateinit var lockKeyGenerator: LockKeyGenerator

    @BeforeEach
    fun setUp() {
        lockKeyGenerator = LockKeyGenerator()
        `when`(joinPoint.signature).thenReturn(methodSignature)
    }

    @Test
    fun `should generate simple key without SpEL`() {
        // Given
        val lock = createLockAnnotation(key = "simple-key", prefix = "test")

        // When
        val result = lockKeyGenerator.generateKey(joinPoint, lock)

        // Then
        assertEquals("test:simple-key", result)
    }

    @Test
    fun `should generate key with SpEL expression using parameter name`() {
        // Given
        val lock = createLockAnnotation(key = "'user:' + #userId", prefix = "lock")
        val method = TestService::class.java.getMethod("testMethod", Long::class.java)
        
        `when`(methodSignature.method).thenReturn(method)
        `when`(joinPoint.args).thenReturn(arrayOf(123L))

        // When
        val result = lockKeyGenerator.generateKey(joinPoint, lock)

        // Then
        assertEquals("lock:user:123", result)
    }

    @Test
    fun `should generate key with SpEL expression using p0 syntax`() {
        // Given
        val lock = createLockAnnotation(key = "'order:' + #p0", prefix = "lock")
        val method = TestService::class.java.getMethod("testMethod", Long::class.java)
        
        `when`(methodSignature.method).thenReturn(method)
        `when`(joinPoint.args).thenReturn(arrayOf(456L))

        // When
        val result = lockKeyGenerator.generateKey(joinPoint, lock)

        // Then
        assertEquals("lock:order:456", result)
    }

    @Test
    fun `should generate key with executionPath variable`() {
        // Given
        val lock = createLockAnnotation(key = "#executionPath", prefix = "method")
        val method = TestService::class.java.getMethod("testMethod", Long::class.java)
        
        `when`(methodSignature.method).thenReturn(method)
        `when`(joinPoint.args).thenReturn(arrayOf(789L))

        // When
        val result = lockKeyGenerator.generateKey(joinPoint, lock)

        // Then
        assertEquals("method:TestService.testMethod", result)
    }

    @Test
    fun `should generate key with array parameter`() {
        // Given
        val lock = createLockAnnotation(key = "'batch:' + #ids", prefix = "lock")
        val method = TestService::class.java.getMethod("batchMethod", Array<String>::class.java)
        
        `when`(methodSignature.method).thenReturn(method)
        `when`(joinPoint.args).thenReturn(arrayOf(arrayOf("1", "2", "3")))

        // When
        val result = lockKeyGenerator.generateKey(joinPoint, lock)

        // Then
        assertEquals("lock:batch:1:2:3", result)
    }

    @Test
    fun `should generate key with list parameter`() {
        // Given
        val lock = createLockAnnotation(key = "'list:' + #items", prefix = "lock")
        val method = TestService::class.java.getMethod("listMethod", List::class.java)
        
        `when`(methodSignature.method).thenReturn(method)
        `when`(joinPoint.args).thenReturn(arrayOf(listOf("a", "b", "c")))

        // When
        val result = lockKeyGenerator.generateKey(joinPoint, lock)

        // Then
        assertEquals("lock:list:a:b:c", result)
    }

    @Test
    fun `should generate fallback key when SpEL evaluation fails`() {
        // Given
        val lock = createLockAnnotation(key = "#invalidExpression", prefix = "fallback")
        val method = TestService::class.java.getMethod("testMethod", Long::class.java)
        
        `when`(methodSignature.method).thenReturn(method)
        `when`(joinPoint.args).thenReturn(arrayOf(123L))

        // When
        val result = lockKeyGenerator.generateKey(joinPoint, lock)

        // Then
        assertEquals("fallback:TestService:testMethod", result)
    }

    @Test
    fun `should generate hash for long keys`() {
        // Given
        val longKey = "a".repeat(300) // 超过最大长度
        val lock = createLockAnnotation(key = longKey, prefix = "long")

        // When
        val result = lockKeyGenerator.generateKey(joinPoint, lock)

        // Then
        assertTrue(result.startsWith("long:hash:"))
        assertTrue(result.length < 250)
    }

    @Test
    fun `should generate static utility keys`() {
        // Test static utility methods
        assertEquals("lock:user:123", LockKeyGenerator.generateUserKey(123L))
        assertEquals("lock:order:ORDER-456", LockKeyGenerator.generateOrderKey("ORDER-456"))
        assertEquals("lock:payment:txn-789", LockKeyGenerator.generateBusinessKey("payment", "txn-789"))
        assertEquals("lock:a:b:c", LockKeyGenerator.generateSimpleKey("lock", "a", "b", "c"))
    }

    // 辅助方法：创建Lock注解实例
    private fun createLockAnnotation(
        key: String,
        prefix: String = "lock",
        waitTime: Long = 10L,
        leaseTime: Long = 30L,
        failMessage: String = "获取分布式锁失败，请稍后重试",
        autoRelease: Boolean = true
    ): Lock {
        return object : Lock {
            override fun annotationClass() = Lock::class
            override val key = key
            override val prefix = prefix
            override val waitTime = waitTime
            override val leaseTime = leaseTime
            override val timeUnit = java.util.concurrent.TimeUnit.SECONDS
            override val failMessage = failMessage
            override val autoRelease = autoRelease
        }
    }

    // 测试用的Service类
    class TestService {
        fun testMethod(userId: Long): String = "test"
        fun batchMethod(ids: Array<String>): String = "batch"
        fun listMethod(items: List<String>): String = "list"
    }
}
