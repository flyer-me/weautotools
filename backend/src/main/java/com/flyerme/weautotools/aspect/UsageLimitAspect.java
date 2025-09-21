package com.flyerme.weautotools.aspect;

import com.flyerme.weautotools.annotation.UsageLimit;
import com.flyerme.weautotools.common.Result;
import com.flyerme.weautotools.dto.AuthInfo;
import com.flyerme.weautotools.entity.ToolUsageLimit;
import com.flyerme.weautotools.service.UsageLimitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 使用限制检查切面
 * 用于拦截带有@UsageLimit注解的方法，进行使用次数限制检查
 * 使用简化的认证中心服务获取用户信息
 *
 * @author WeAutoTools Team
 * @version 1.0.4
 * @since 2025-09-12
 */
@Aspect
@RequiredArgsConstructor
@Slf4j
public class UsageLimitAspect {

    private final UsageLimitService usageLimitService;

    /**
     * 环绕通知，检查使用限制
     * 使用简化的认证服务获取用户信息
     */
    @Around("@annotation(usageLimit)")
    public Object checkUsageLimit(ProceedingJoinPoint joinPoint, UsageLimit usageLimit) throws Throwable {
        try {
            // 获取HTTP请求信息
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                log.warn("无法获取HTTP请求信息，跳过使用限制检查");
                return joinPoint.proceed();
            }

            // 获取用户信息
            AuthInfo authInfo = AuthInfo.ANONYMOUS; // TODO
            String userIdentifier = authInfo.userIdentifier();
            ToolUsageLimit.UserType userType = authInfo.authenticated() ?
                ToolUsageLimit.UserType.LOGIN : ToolUsageLimit.UserType.ANONYMOUS;
            
            log.debug("检测到用户: {}, 类型: {}", userIdentifier, userType);
            
            // 检查使用限制
            boolean isExceeded = usageLimitService.isExceededLimit(
                userIdentifier, 
                usageLimit.toolName(), 
                userType
            );
            
            if (isExceeded) {
                log.warn("用户 {} 使用工具 {} 超出限制", userIdentifier, usageLimit.toolName());
                return createLimitExceededResponse(usageLimit);
            }
            
            // 记录使用行为
            usageLimitService.recordUsage(userIdentifier, usageLimit.toolName());
            
            log.debug("用户 {} 使用工具 {} 检查通过", userIdentifier, usageLimit.toolName());
            
            // 继续执行原方法
            return joinPoint.proceed();
            
        } catch (Exception e) {
            log.error("使用限制检查发生异常", e);
            // 如果检查过程出现异常，允许继续执行（避免因为限制检查导致业务中断）
            return joinPoint.proceed();
        }
    }
    
    /**
     * 创建限制超出的响应
     */
    private Result<?> createLimitExceededResponse(UsageLimit usageLimit) {
        return Result.error(
            HttpStatus.TOO_MANY_REQUESTS.value(), 
            usageLimit.message().isEmpty() ? "使用次数已达限制，请稍后再试或登录获取更多使用次数" : usageLimit.message()
        );
    }
}