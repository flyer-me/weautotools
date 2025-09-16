package com.flyerme.weautotools.service.impl;

import com.flyerme.weautotools.dao.ToolUsageLimitMapper;
import com.flyerme.weautotools.dao.ToolUsageRecordMapper;
import com.flyerme.weautotools.entity.ToolUsageLimit;
import com.flyerme.weautotools.entity.ToolUsageRecord;
import com.flyerme.weautotools.service.UsageLimitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 使用限制服务实现
 *
 * @author WeAutoTools Team
 * @version 1.0.2
 * @since 2025-09-12
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UsageLimitServiceImpl implements UsageLimitService {

    private final ToolUsageLimitMapper toolUsageLimitMapper;
    private final ToolUsageRecordMapper toolUsageRecordMapper;
    private final StringRedisTemplate redisTemplate;

    @Override
    public boolean isExceededLimit(String userIdentifier, String toolName, ToolUsageLimit.UserType userType) {
        try {
            // 获取限制配置
            ToolUsageLimit dailyLimit = getUsageLimitByType(toolName, userType, ToolUsageLimit.LimitType.DAILY);
            ToolUsageLimit hourlyLimit = getUsageLimitByType(toolName, userType, ToolUsageLimit.LimitType.HOURLY);

            // 检查每日限制
            if (dailyLimit != null && isExceededDailyLimit(userIdentifier, toolName, dailyLimit.getLimitCount())) {
                log.debug("用户 {} 工具 {} 超出每日限制", userIdentifier, toolName);
                return true;
            }

            // 检查每小时限制
            if (hourlyLimit != null && isExceededHourlyLimit(userIdentifier, toolName, hourlyLimit.getLimitCount())) {
                log.debug("用户 {} 工具 {} 超出每小时限制", userIdentifier, toolName);
                return true;
            }

            return false;
        } catch (Exception e) {
            log.error("检查使用限制(UsageLimitService)异常", e);
            // 异常情况下允许使用，避免因为限制检查导致业务中断
            return false;
        }
    }

    @Override
    public void recordUsage(String userIdentifier, String toolName, ToolUsageLimit.UserType userType, 
                           Long userId, String ipAddress, String userAgent) {
        try {
            // 更新Redis计数器
            incrementRedisCounter(userIdentifier, toolName, "daily");
            incrementRedisCounter(userIdentifier, toolName, "hourly");

            // 异步记录到数据库
            recordToDatabase(userIdentifier, toolName, userId, ipAddress, userAgent);
            
            log.debug("记录用户 {} 使用工具 {}", userIdentifier, toolName);
        } catch (Exception e) {
            log.error("记录使用行为异常", e);
        }
    }

    @Override
    public int getRemainingUsage(String userIdentifier, String toolName, ToolUsageLimit.UserType userType) {
        try {
            ToolUsageLimit dailyLimit = getUsageLimitByType(toolName, userType, ToolUsageLimit.LimitType.DAILY);
            if (dailyLimit == null) {
                return Integer.MAX_VALUE;
            }

            int dailyUsed = getDailyUsageCount(userIdentifier, toolName);
            return Math.max(0, dailyLimit.getLimitCount() - dailyUsed);
        } catch (Exception e) {
            log.error("获取剩余使用次数异常", e);
            return 0;
        }
    }

    @Override
    public void resetUserUsage(String userIdentifier) {
        try {
            String pattern = String.format("limit:%s:*", userIdentifier.replace(":", "\\:"));
            redisTemplate.delete(redisTemplate.keys(pattern));
            log.info("重置用户 {} 的使用次数", userIdentifier);
        } catch (Exception e) {
            log.error("重置用户使用次数异常", e);
        }
    }

    @Override
    public ToolUsageLimit getUsageLimit(String toolName, ToolUsageLimit.UserType userType) {
        return getUsageLimitByType(toolName, userType, ToolUsageLimit.LimitType.DAILY);
    }

    /**
     * 根据限制类型获取使用限制配置
     */
    private ToolUsageLimit getUsageLimitByType(String toolName, ToolUsageLimit.UserType userType, 
                                              ToolUsageLimit.LimitType limitType) {
        // 1. 先查找具体工具的限制配置
        List<ToolUsageLimit> limits = toolUsageLimitMapper.selectByToolNameAndUserType(
            toolName, userType.name());
        
        ToolUsageLimit specificLimit = limits.stream()
            .filter(limit -> limitType.name().equals(limit.getLimitType()))
            .findFirst()
            .orElse(null);
        
        if (specificLimit != null) {
            return specificLimit;
        }

        // 2. 查找工具类型的默认限制
        String toolType = extractToolType(toolName);
        List<ToolUsageLimit> typeLimits = toolUsageLimitMapper.selectByToolTypeAndUserType(
            toolType, userType.name());

        ToolUsageLimit typeLimit = typeLimits.stream()
            .filter(limit -> limitType.name().equals(limit.getLimitType()))
            .findFirst()
            .orElse(null);

        if (typeLimit != null) {
            return typeLimit;
        }

        // 3. 使用系统默认限制
        List<ToolUsageLimit> defaultLimits = toolUsageLimitMapper.selectDefaultLimits(userType.name());
        return defaultLimits.stream()
            .filter(limit -> limitType.name().equals(limit.getLimitType()))
            .findFirst()
            .orElse(null);
    }

    /**
     * 从工具名称提取工具类型
     */
    private String extractToolType(String toolName) {
        //TODO: 业务扩展后添加不同的工具类型
        return "DEFAULT";
    }

    /**
     * 检查是否超出每日限制
     */
    private boolean isExceededDailyLimit(String userIdentifier, String toolName, int dailyLimit) {
        int dailyUsed = getDailyUsageCount(userIdentifier, toolName);
        return dailyUsed >= dailyLimit;
    }

    /**
     * 检查是否超出每小时限制
     */
    private boolean isExceededHourlyLimit(String userIdentifier, String toolName, int hourlyLimit) {
        int hourlyUsed = getHourlyUsageCount(userIdentifier, toolName);
        return hourlyUsed >= hourlyLimit;
    }

    /**
     * 获取每日使用次数
     */
    private int getDailyUsageCount(String userIdentifier, String toolName) {
        String key = buildRedisKey(userIdentifier, toolName, "daily");
        String count = redisTemplate.opsForValue().get(key);
        return count != null ? Integer.parseInt(count) : 0;
    }

    /**
     * 获取每小时使用次数
     */
    private int getHourlyUsageCount(String userIdentifier, String toolName) {
        String key = buildRedisKey(userIdentifier, toolName, "hourly");
        String count = redisTemplate.opsForValue().get(key);
        return count != null ? Integer.parseInt(count) : 0;
    }

    /**
     * 增加Redis计数器
     */
    private void incrementRedisCounter(String userIdentifier, String toolName, String timeType) {
        String key = buildRedisKey(userIdentifier, toolName, timeType);
        
        // 增加计数器
        Long newCount = redisTemplate.opsForValue().increment(key);
        
        // 设置过期时间
        if (newCount != null && newCount == 1) {
            if ("daily".equals(timeType)) {
                // 每日计数器，设置为第二天零点过期
                long secondsUntilMidnight = ChronoUnit.SECONDS.between(
                    LocalDateTime.now(), 
                    LocalDateTime.now().plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0)
                );
                redisTemplate.expire(key, secondsUntilMidnight, TimeUnit.SECONDS);
            } else if ("hourly".equals(timeType)) {
                // 每小时计数器，设置为下一小时过期
                long secondsUntilNextHour = ChronoUnit.SECONDS.between(
                    LocalDateTime.now(), 
                    LocalDateTime.now().plusHours(1).withMinute(0).withSecond(0).withNano(0)
                );
                redisTemplate.expire(key, secondsUntilNextHour, TimeUnit.SECONDS);
            }
        }
    }

    /**
     * 构建Redis键
     */
    private String buildRedisKey(String userIdentifier, String toolName, String timeType) {
        return String.format("limit:%s:%s:%s", userIdentifier, toolName, timeType);
    }

    /**
     * 记录到数据库
     */
    private void recordToDatabase(String userIdentifier, String toolName,
                                  Long userId, String ipAddress, String userAgent) {
        ToolUsageRecord record = new ToolUsageRecord();
        record.setUserId(userId);
        record.setUserIdentifier(userIdentifier);
        record.setToolType(extractToolType(toolName));
        record.setToolName(toolName);
        record.setUsageTime(LocalDateTime.now());
        record.setIpAddress(ipAddress);
        record.setUserAgent(userAgent);
        
        toolUsageRecordMapper.insert(record);
    }
}