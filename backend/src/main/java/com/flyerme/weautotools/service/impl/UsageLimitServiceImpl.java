package com.flyerme.weautotools.service.impl;

import com.flyerme.weautotools.dao.ToolUsageLimitMapper;
import com.flyerme.weautotools.entity.ToolUsageLimit;
import com.flyerme.weautotools.service.ToolService;
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
    private final StringRedisTemplate redisTemplate;
    private final ToolService toolService;

    @Override
    public boolean isExceededLimit(String userIdentifier, String toolName, ToolUsageLimit.UserType userType) {
        try {
            // 1. 获得tool_id
            Long toolId = toolService.getToolIdByName(toolName);
            if (toolId == null) {
                log.warn("工具不存在: {}, 使用默认限制策略", toolName);
                return isExceededDefaultLimit(userIdentifier, userType);
            }

            // 2. 获取限制配置（优先使用tool_id查询）
            ToolUsageLimit dailyLimit = getUsageLimitByToolIdAndType(toolId, userType, ToolUsageLimit.LimitType.DAILY);

            // 3. 检查每日限制
            if (dailyLimit != null && isExceededDailyLimit(userIdentifier, toolName, dailyLimit.getLimitCount())) {
                log.debug("用户 {} 工具 {} 超出每日限制", userIdentifier, toolName);
                return true;
            }

            ToolUsageLimit hourlyLimit = getUsageLimitByToolIdAndType(toolId, userType, ToolUsageLimit.LimitType.HOURLY);

            // 4. 检查每小时限制
            if (hourlyLimit != null && isExceededHourlyLimit(userIdentifier, toolName, hourlyLimit.getLimitCount())) {
                log.debug("用户 {} 工具 {} 超出每小时限制", userIdentifier, toolName);
                return true;
            }

            return false;
        } catch (Exception e) {
            log.error("检查使用限制异常，工具: {}, 用户: {}", toolName, userIdentifier, e);
            // 异常情况下允许使用，避免因为限制检查导致业务中断
            return false;
        }
    }

    @Override
    public void recordUsage(String userIdentifier, String toolName) {
        try {
            // 1. 获取工具ID
            Long toolId = toolService.getToolIdByName(toolName);
            if (toolId == null) {
                log.warn("工具不存在: {}, 跳过使用记录", toolName);
                return;
            }

            // 2. 更新Redis计数器（保持现有递增逻辑）
            incrementRedisCounter(userIdentifier, toolName, "daily");
            incrementRedisCounter(userIdentifier, toolName, "hourly");
            
            log.debug("记录用户 {} 使用工具 {} (ID: {})", userIdentifier, toolName, toolId);
        } catch (Exception e) {
            log.error("记录使用行为异常，工具: {}, 用户: {}", toolName, userIdentifier, e);
        }
    }

    @Override
    public int getRemainingUsage(String userIdentifier, String toolName, ToolUsageLimit.UserType userType) {
        try {
            // 1. 获取工具ID
            Long toolId = toolService.getToolIdByName(toolName);
            if (toolId == null) {
                log.warn("工具不存在: {}, 返回默认限制", toolName);
                return getDefaultRemainingUsage(userIdentifier, userType);
            }

            // 2. 获取每日限制配置
            ToolUsageLimit dailyLimit = getUsageLimitByToolIdAndType(toolId, userType, ToolUsageLimit.LimitType.DAILY);
            if (dailyLimit == null) {
                return Integer.MAX_VALUE;
            }

            // 3. 计算剩余使用次数
            int dailyUsed = getDailyUsageCount(userIdentifier, toolName);
            return Math.max(0, dailyLimit.getLimitCount() - dailyUsed);
        } catch (Exception e) {
            log.error("获取剩余使用次数异常，工具: {}, 用户: {}", toolName, userIdentifier, e);
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
        Long toolId = toolService.getToolIdByName(toolName);
        if (toolId == null) {
            log.warn("工具不存在: {}, 返回默认限制", toolName);
            return getDefaultUsageLimit(userType);
        }
        return getUsageLimitByToolIdAndType(toolId, userType, ToolUsageLimit.LimitType.DAILY);
    }

    /**
     * 根据限制类型获取使用限制配置
     */
    private ToolUsageLimit getUsageLimitByToolIdAndType(Long toolId, ToolUsageLimit.UserType userType, 
                                                        ToolUsageLimit.LimitType limitType) {
        // 1. 先查找具体工具的限制配置（优先使用tool_id查询）
        List<ToolUsageLimit> limits = toolUsageLimitMapper.selectByToolIdAndUserType(toolId, userType.name());
        
        ToolUsageLimit specificLimit = limits.stream()
            .filter(limit -> limitType.name().equals(limit.getLimitType()))
            .findFirst()
            .orElse(null);
        
        if (specificLimit != null) {
            return specificLimit;
        }

        return getDefaultUsageLimitByType(userType, limitType);
    }

    /**
     * 获取默认使用限制（按类型）
     */
    private ToolUsageLimit getDefaultUsageLimitByType(ToolUsageLimit.UserType userType, ToolUsageLimit.LimitType limitType) {
        List<ToolUsageLimit> defaultLimits = toolUsageLimitMapper.selectDefaultLimits(userType.name());
        return defaultLimits.stream()
            .filter(limit -> limitType.name().equals(limit.getLimitType()))
            .findFirst()
            .orElse(null);
    }

    /**
     * 获取默认使用限制（返回每日限制）
     */
    private ToolUsageLimit getDefaultUsageLimit(ToolUsageLimit.UserType userType) {
        return getDefaultUsageLimitByType(userType, ToolUsageLimit.LimitType.DAILY);
    }

    /**
     * 检查是否超出默认限制
     */
    private boolean isExceededDefaultLimit(String userIdentifier, ToolUsageLimit.UserType userType) {
        ToolUsageLimit dailyLimit = getDefaultUsageLimit(userType);
        if (dailyLimit == null) {
            return false; // 没有默认限制，不限制使用
        }
        return getDailyUsageCount(userIdentifier, "default") >= dailyLimit.getLimitCount();
    }

    /**
     * 获取默认剩余使用次数
     */
    private int getDefaultRemainingUsage(String userIdentifier, ToolUsageLimit.UserType userType) {
        ToolUsageLimit dailyLimit = getDefaultUsageLimit(userType);
        if (dailyLimit == null) {
            return Integer.MAX_VALUE;
        }
        int dailyUsed = getDailyUsageCount(userIdentifier, "default");
        return Math.max(0, dailyLimit.getLimitCount() - dailyUsed);
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
        String key = buildRedisKey(userIdentifier, toolName, "hourly");
        String count = redisTemplate.opsForValue().get(key);
        int hourlyUsed = count != null ? Integer.parseInt(count) : 0;
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
        return String.format("usage-limit:%s:%s:%s", userIdentifier, toolName, timeType);
    }

}