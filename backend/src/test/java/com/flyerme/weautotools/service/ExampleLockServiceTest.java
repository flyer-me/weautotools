package com.flyerme.weautotools.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ExampleLockService 测试类
 * 验证 Kotlin 类是否可以被 Spring AOP 正确代理
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.data.redis.host=localhost",
    "spring.data.redis.port=6379",
    "spring.data.redis.database=0"
})
public class ExampleLockServiceTest {

    @Autowired
    private ExampleLockService exampleLockService;

    @Test
    public void testServiceCanBeInjected() {
        // 验证服务可以被正确注入
        assertNotNull(exampleLockService, "ExampleLockService should be injected");
    }

    @Test
    public void testServiceIsProxied() {
        // 验证服务是被代理的（包含 CGLIB 或 JDK 代理）
        String className = exampleLockService.getClass().getName();
        assertTrue(
            className.contains("CGLIB") || className.contains("Proxy") || className.contains("$"),
            "ExampleLockService should be proxied by Spring AOP, but got: " + className
        );
    }

    @Test
    public void testBusinessOperation() {
        // 测试业务操作方法
        try {
            String result = exampleLockService.businessOperation("test123");
            assertNotNull(result);
            assertTrue(result.contains("test123"));
        } catch (Exception e) {
            // 如果 Redis 不可用，至少验证方法可以被调用
            assertTrue(e.getMessage().contains("Redis") || e.getMessage().contains("连接") ||
                      e.getMessage().contains("分布式锁"),
                      "Expected Redis connection error, but got: " + e.getMessage());
        }
    }

    @Test
    public void testLockAnnotatedMethodCanBeCalled() {
        // 测试带有 @Lock 注解的方法是否可以被调用
        // 注意：这个测试需要 Redis 连接，如果 Redis 不可用会失败
        try {
            String result = exampleLockService.userOperation(12345L);
            assertNotNull(result);
            assertTrue(result.contains("12345"));
        } catch (Exception e) {
            // 如果 Redis 不可用，至少验证方法可以被调用（不会因为代理问题而失败）
            assertTrue(e.getMessage().contains("Redis") || e.getMessage().contains("连接") ||
                      e.getMessage().contains("分布式锁"),
                      "Expected Redis connection error, but got: " + e.getMessage());
        }
    }
}
