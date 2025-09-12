package com.flyerme.weautotools.service;

import com.flyerme.weautotools.dao.ToolUsageLimitMapper;
import com.flyerme.weautotools.dao.ToolUsageRecordMapper;
import com.flyerme.weautotools.entity.ToolUsageLimit;
import com.flyerme.weautotools.entity.ToolUsageRecord;
import com.flyerme.weautotools.service.impl.UsageLimitServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 使用限制服务单元测试
 *
 * @author WeAutoTools Team
 * @version 1.0.2
 * @since 2025-09-12
 */
@ExtendWith(MockitoExtension.class)
class UsageLimitServiceTest {

    @Mock
    private ToolUsageLimitMapper toolUsageLimitMapper;

    @Mock
    private ToolUsageRecordMapper toolUsageRecordMapper;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private UsageLimitServiceImpl usageLimitService;

    private ToolUsageLimit dailyLimit;
    private ToolUsageLimit hourlyLimit;

    @BeforeEach
    void setUp() {
        // 设置模拟的限制配置
        dailyLimit = new ToolUsageLimit();
        dailyLimit.setId(1L);
        dailyLimit.setToolName("qr-generate");
        dailyLimit.setUserType("ANONYMOUS");
        dailyLimit.setLimitType("DAILY");
        dailyLimit.setLimitCount(10);
        dailyLimit.setEnabled(true);

        hourlyLimit = new ToolUsageLimit();
        hourlyLimit.setId(2L);
        hourlyLimit.setToolName("qr-generate");
        hourlyLimit.setUserType("ANONYMOUS");
        hourlyLimit.setLimitType("HOURLY");
        hourlyLimit.setLimitCount(3);
        hourlyLimit.setEnabled(true);

        // 设置Redis模拟
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void testIsExceededLimit_NotExceeded() {
        // 安排
        String userIdentifier = "anonymous:test123";
        String toolName = "qr-generate";
        ToolUsageLimit.UserType userType = ToolUsageLimit.UserType.ANONYMOUS;

        List<ToolUsageLimit> dailyLimits = Arrays.asList(dailyLimit);
        List<ToolUsageLimit> hourlyLimits = Arrays.asList(hourlyLimit);

        when(toolUsageLimitMapper.selectByToolNameAndUserType(toolName, userType.name()))
            .thenReturn(dailyLimits);
        when(valueOperations.get("limit:anonymous:test123:qr-generate:daily")).thenReturn("5");
        when(valueOperations.get("limit:anonymous:test123:qr-generate:hourly")).thenReturn("2");

        // 执行
        boolean result = usageLimitService.isExceededLimit(userIdentifier, toolName, userType);

        // 验证
        assertFalse(result);
    }

    @Test
    void testIsExceededLimit_ExceededDaily() {
        // 安排
        String userIdentifier = "anonymous:test123";
        String toolName = "qr-generate";
        ToolUsageLimit.UserType userType = ToolUsageLimit.UserType.ANONYMOUS;

        List<ToolUsageLimit> dailyLimits = Arrays.asList(dailyLimit);

        when(toolUsageLimitMapper.selectByToolNameAndUserType(toolName, userType.name()))
            .thenReturn(dailyLimits);
        when(valueOperations.get("limit:anonymous:test123:qr-generate:daily")).thenReturn("10");

        // 执行
        boolean result = usageLimitService.isExceededLimit(userIdentifier, toolName, userType);

        // 验证
        assertTrue(result);
    }

    @Test
    void testIsExceededLimit_ExceededHourly() {
        // 安排
        String userIdentifier = "anonymous:test123";
        String toolName = "qr-generate";
        ToolUsageLimit.UserType userType = ToolUsageLimit.UserType.ANONYMOUS;

        List<ToolUsageLimit> dailyLimits = Arrays.asList(dailyLimit);
        List<ToolUsageLimit> hourlyLimits = Arrays.asList(hourlyLimit);

        when(toolUsageLimitMapper.selectByToolNameAndUserType(toolName, userType.name()))
            .thenReturn(dailyLimits);
        when(valueOperations.get("limit:anonymous:test123:qr-generate:daily")).thenReturn("5");
        when(valueOperations.get("limit:anonymous:test123:qr-generate:hourly")).thenReturn("3");

        // 执行
        boolean result = usageLimitService.isExceededLimit(userIdentifier, toolName, userType);

        // 验证
        assertTrue(result);
    }

    @Test
    void testRecordUsage() {
        // 安排
        String userIdentifier = "user:123";
        String toolName = "qr-generate";
        ToolUsageLimit.UserType userType = ToolUsageLimit.UserType.LOGIN;
        Long userId = 123L;
        String ipAddress = "192.168.1.1";
        String userAgent = "Mozilla/5.0";

        when(valueOperations.increment(anyString())).thenReturn(1L);

        // 执行
        usageLimitService.recordUsage(userIdentifier, toolName, userType, userId, ipAddress, userAgent);

        // 验证
        verify(valueOperations, times(2)).increment(anyString()); // daily and hourly
        verify(toolUsageRecordMapper, times(1)).insert((ToolUsageRecord) any());
    }

    @Test
    void testGetRemainingUsage() {
        // 安排
        String userIdentifier = "anonymous:test123";
        String toolName = "qr-generate";
        ToolUsageLimit.UserType userType = ToolUsageLimit.UserType.ANONYMOUS;

        List<ToolUsageLimit> dailyLimits = Arrays.asList(dailyLimit);

        when(toolUsageLimitMapper.selectByToolNameAndUserType(toolName, userType.name()))
            .thenReturn(dailyLimits);
        when(valueOperations.get("limit:anonymous:test123:qr-generate:daily")).thenReturn("7");

        // 执行
        int remaining = usageLimitService.getRemainingUsage(userIdentifier, toolName, userType);

        // 验证
        assertEquals(3, remaining);
    }

    @Test
    void testGetRemainingUsage_NoUsage() {
        // 安排
        String userIdentifier = "anonymous:test123";
        String toolName = "qr-generate";
        ToolUsageLimit.UserType userType = ToolUsageLimit.UserType.ANONYMOUS;

        List<ToolUsageLimit> dailyLimits = Arrays.asList(dailyLimit);

        when(toolUsageLimitMapper.selectByToolNameAndUserType(toolName, userType.name()))
            .thenReturn(dailyLimits);
        when(valueOperations.get("limit:anonymous:test123:qr-generate:daily")).thenReturn(null);

        // 执行
        int remaining = usageLimitService.getRemainingUsage(userIdentifier, toolName, userType);

        // 验证
        assertEquals(10, remaining);
    }

    @Test
    void testResetUserUsage() {
        // 安排
        String userIdentifier = "anonymous:test123";

        // 执行
        usageLimitService.resetUserUsage(userIdentifier);

        // 验证
        verify(redisTemplate, times(1)).keys(anyString());
        verify(redisTemplate, times(1)).delete((Set<String>) any());
    }

    @Test
    void testGetUsageLimit() {
        // 安排
        String toolName = "qr-generate";
        ToolUsageLimit.UserType userType = ToolUsageLimit.UserType.ANONYMOUS;

        List<ToolUsageLimit> dailyLimits = Arrays.asList(dailyLimit);

        when(toolUsageLimitMapper.selectByToolNameAndUserType(toolName, userType.name()))
            .thenReturn(dailyLimits);

        // 执行
        ToolUsageLimit result = usageLimitService.getUsageLimit(toolName, userType);

        // 验证
        assertNotNull(result);
        assertEquals(toolName, result.getToolName());
        assertEquals(userType.name(), result.getUserType());
        assertEquals("DAILY", result.getLimitType());
        assertEquals(10, result.getLimitCount());
    }

    @Test
    void testGetUsageLimit_DefaultFallback() {
        // 安排
        String toolName = "unknown-tool";
        ToolUsageLimit.UserType userType = ToolUsageLimit.UserType.ANONYMOUS;

        ToolUsageLimit defaultLimit = new ToolUsageLimit();
        defaultLimit.setId(3L);
        defaultLimit.setToolName("default");
        defaultLimit.setUserType("ANONYMOUS");
        defaultLimit.setLimitType("DAILY");
        defaultLimit.setLimitCount(5);
        defaultLimit.setEnabled(true);

        when(toolUsageLimitMapper.selectByToolNameAndUserType(toolName, userType.name()))
            .thenReturn(Arrays.asList());
        when(toolUsageLimitMapper.selectByToolTypeAndUserType(any(), eq(userType.name())))
            .thenReturn(Arrays.asList());
        when(toolUsageLimitMapper.selectDefaultLimits(userType.name()))
            .thenReturn(Arrays.asList(defaultLimit));

        // 执行
        ToolUsageLimit result = usageLimitService.getUsageLimit(toolName, userType);

        // 验证
        assertNotNull(result);
        assertEquals("default", result.getToolName());
        assertEquals(5, result.getLimitCount());
    }
}