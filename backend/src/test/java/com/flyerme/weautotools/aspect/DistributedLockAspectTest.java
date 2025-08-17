package com.flyerme.weautotools.aspect;

import com.flyerme.weautotools.annotation.DistributedLock;
import com.flyerme.weautotools.exception.BusinessException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.DefaultParameterNameDiscoverer;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 分布式锁切面测试
 *
 * @author WeAutoTools Team
 * @version 1.0.0
 * @since 2025-08-17
 */
@ExtendWith(MockitoExtension.class)
class DistributedLockAspectTest {

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private RLock rLock;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    @InjectMocks
    private DistributedLockAspect distributedLockAspect;

    private DistributedLock distributedLockAnnotation;

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        when(redissonClient.getLock(anyString())).thenReturn(rLock);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        
        // 创建测试注解
        distributedLockAnnotation = new DistributedLock() {
            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return DistributedLock.class;
            }

            @Override
            public String key() {
                return "test:key";
            }

            @Override
            public String prefix() {
                return "lock";
            }

            @Override
            public long waitTime() {
                return 10L;
            }

            @Override
            public long leaseTime() {
                return 30L;
            }

            @Override
            public TimeUnit timeUnit() {
                return TimeUnit.SECONDS;
            }

            @Override
            public String failMessage() {
                return "获取分布式锁失败，请稍后重试";
            }

            @Override
            public boolean autoRelease() {
                return true;
            }
        };
    }

    @Test
    void testAround_Success() throws Throwable {
        // Given
        String expectedResult = "success";
        when(rLock.tryLock(10L, 30L, TimeUnit.SECONDS)).thenReturn(true);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);
        when(joinPoint.proceed()).thenReturn(expectedResult);

        // When
        Object result = distributedLockAspect.around(joinPoint, distributedLockAnnotation);

        // Then
        assertEquals(expectedResult, result);
        verify(rLock).tryLock(10L, 30L, TimeUnit.SECONDS);
        verify(joinPoint).proceed();
        verify(rLock).unlock();
    }

    @Test
    void testAround_FailToAcquireLock() throws Throwable {
        // Given
        when(rLock.tryLock(10L, 30L, TimeUnit.SECONDS)).thenReturn(false);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            distributedLockAspect.around(joinPoint, distributedLockAnnotation);
        });

        assertEquals("获取分布式锁失败，请稍后重试", exception.getMessage());
        verify(joinPoint, never()).proceed();
        verify(rLock, never()).unlock();
    }

    @Test
    void testAround_InterruptedException() throws Throwable {
        // Given
        when(rLock.tryLock(10L, 30L, TimeUnit.SECONDS))
                .thenThrow(new InterruptedException("Test interruption"));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            distributedLockAspect.around(joinPoint, distributedLockAnnotation);
        });

        assertEquals("获取分布式锁被中断", exception.getMessage());
        assertTrue(Thread.currentThread().isInterrupted());
    }

    @Test
    void testAround_AutoReleaseDisabled() throws Throwable {
        // Given
        DistributedLock noAutoReleaseAnnotation = new DistributedLock() {
            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return DistributedLock.class;
            }

            @Override
            public String key() {
                return "test:key";
            }

            @Override
            public String prefix() {
                return "lock";
            }

            @Override
            public long waitTime() {
                return 10L;
            }

            @Override
            public long leaseTime() {
                return 30L;
            }

            @Override
            public TimeUnit timeUnit() {
                return TimeUnit.SECONDS;
            }

            @Override
            public String failMessage() {
                return "获取分布式锁失败，请稍后重试";
            }

            @Override
            public boolean autoRelease() {
                return false;
            }
        };

        String expectedResult = "success";
        when(rLock.tryLock(10L, 30L, TimeUnit.SECONDS)).thenReturn(true);
        when(joinPoint.proceed()).thenReturn(expectedResult);

        // When
        Object result = distributedLockAspect.around(joinPoint, noAutoReleaseAnnotation);

        // Then
        assertEquals(expectedResult, result);
        verify(rLock, never()).unlock();
    }

    @Test
    void testAround_LockNotHeldByCurrentThread() throws Throwable {
        // Given
        String expectedResult = "success";
        when(rLock.tryLock(10L, 30L, TimeUnit.SECONDS)).thenReturn(true);
        when(rLock.isHeldByCurrentThread()).thenReturn(false);
        when(joinPoint.proceed()).thenReturn(expectedResult);

        // When
        Object result = distributedLockAspect.around(joinPoint, distributedLockAnnotation);

        // Then
        assertEquals(expectedResult, result);
        verify(rLock, never()).unlock();
    }

    @Test
    void testGenerateLockKey_SimpleKey() throws Throwable {
        // Given
        DistributedLock simpleKeyAnnotation = new DistributedLock() {
            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return DistributedLock.class;
            }

            @Override
            public String key() {
                return "simple";
            }

            @Override
            public String prefix() {
                return "test";
            }

            @Override
            public long waitTime() {
                return 10L;
            }

            @Override
            public long leaseTime() {
                return 30L;
            }

            @Override
            public TimeUnit timeUnit() {
                return TimeUnit.SECONDS;
            }

            @Override
            public String failMessage() {
                return "fail";
            }

            @Override
            public boolean autoRelease() {
                return true;
            }
        };

        when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);
        when(joinPoint.proceed()).thenReturn("result");

        // When
        distributedLockAspect.around(joinPoint, simpleKeyAnnotation);

        // Then
        verify(redissonClient).getLock("test:simple");
    }

    @Test
    void testGenerateLockKey_SpELExpression() throws Throwable {
        // Given
        DistributedLock spelAnnotation = new DistributedLock() {
            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return DistributedLock.class;
            }

            @Override
            public String key() {
                return "'user:' + #userId";
            }

            @Override
            public String prefix() {
                return "lock";
            }

            @Override
            public long waitTime() {
                return 10L;
            }

            @Override
            public long leaseTime() {
                return 30L;
            }

            @Override
            public TimeUnit timeUnit() {
                return TimeUnit.SECONDS;
            }

            @Override
            public String failMessage() {
                return "fail";
            }

            @Override
            public boolean autoRelease() {
                return true;
            }
        };

        // Mock method signature for SpEL parsing
        try {
            Method testMethod = TestService.class.getMethod("testMethod", Long.class);
            when(methodSignature.getMethod()).thenReturn(testMethod);
            when(joinPoint.getArgs()).thenReturn(new Object[]{123L});
        } catch (NoSuchMethodException e) {
            fail("Test method not found", e);
        }

        when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);
        when(joinPoint.proceed()).thenReturn("result");

        // When
        distributedLockAspect.around(joinPoint, spelAnnotation);

        // Then
        verify(redissonClient).getLock("lock:user:123");
    }

    // 测试用的Service类
    public static class TestService {
        public String testMethod(Long userId) {
            return "test";
        }
    }
}
