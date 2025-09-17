package com.flyerme.weautotools.aspect;

import com.flyerme.weautotools.annotation.UsageLimit;
import com.flyerme.weautotools.auth.AuthConstants;
import com.flyerme.weautotools.auth.AuthenticationCenterService;
import com.flyerme.weautotools.common.Result;
import com.flyerme.weautotools.dto.AuthenticatedUser;
import com.flyerme.weautotools.entity.ToolUsageLimit;
import com.flyerme.weautotools.service.UsageLimitService;
import com.flyerme.weautotools.util.IpUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 使用限制检查切面
 * 用于拦截带有@UsageLimit注解的方法，进行使用次数限制检查
 * 使用统一的认证中心服务获取用户信息
 *
 * @author WeAutoTools Team
 * @version 1.0.3
 * @since 2025-09-12
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class UsageLimitAspect {

    private final UsageLimitService usageLimitService;
    private final AuthenticationCenterService authenticationCenterService;

    /**
     * 环绕通知，检查使用限制
     * 使用统一认证服务获取用户信息
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
            
            HttpServletRequest request = attributes.getRequest();
            
            // 使用统一认证服务获取用户信息
            AuthenticatedUser user = authenticationCenterService.getCurrentUser(request);
            if (user == null) {
                log.warn("无法获取用户信息，创建匿名用户");
                user = createFallbackAnonymousUser(request);
            }
            
            // 获取用户标识和类型
            String userIdentifier = user.getUserIdentifier();
            ToolUsageLimit.UserType userType = convertToUsageLimitUserType(user);
            
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
     * 将AuthenticatedUser的用户类型转换为ToolUsageLimit的用户类型
     */
    private ToolUsageLimit.UserType convertToUsageLimitUserType(AuthenticatedUser user) {
        if (user == null || !user.isAuthenticated()) {
            return ToolUsageLimit.UserType.ANONYMOUS;
        }
        
        // 基于用户类型进行转换
        String userType = user.getUserType();
        if (AuthConstants.ROLE_USER.equals(userType) || AuthConstants.ROLE_ADMIN.equals(userType)) {
            return ToolUsageLimit.UserType.LOGIN;
        }
        
        return ToolUsageLimit.UserType.ANONYMOUS;
    }
    
    /**
     * 创建备用匿名用户（当认证服务返回null时）
     */
    private AuthenticatedUser createFallbackAnonymousUser(HttpServletRequest request) {
        String ipAddress = IpUtils.getClientIpAddress(request);
        return AuthenticatedUser.builder()
                .userType(AuthConstants.ROLE_ANONYMOUS)
                .clientIp(ipAddress)
                .build();
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