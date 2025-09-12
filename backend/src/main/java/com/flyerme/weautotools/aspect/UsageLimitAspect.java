package com.flyerme.weautotools.aspect;

import com.flyerme.weautotools.annotation.UsageLimit;
import com.flyerme.weautotools.auth.JwtTokenProvider;
import com.flyerme.weautotools.common.Result;
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
 *
 * @author WeAutoTools Team
 * @version 1.0.2
 * @since 2025-09-12
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class UsageLimitAspect {

    private final UsageLimitService usageLimitService;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 环绕通知，检查使用限制
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
            String ipAddress = IpUtils.getClientIpAddress(request);
            String userAgent = request.getHeader("User-Agent");
            
            // 获取用户身份信息
            String userIdentifier;
            ToolUsageLimit.UserType userType;
            Long userId = null;
            
            // 尝试从JWT Token获取用户信息
            String token = JwtTokenProvider.extractTokenFromRequest(request);
            if (token != null && jwtTokenProvider.validateToken(token)) {
                userId = jwtTokenProvider.getUserIdFromJWT(token);
                userIdentifier = "user:" + userId;
                userType = ToolUsageLimit.UserType.LOGIN;
                log.debug("检测到登录用户: {}", userId);
            } else {
                // 匿名用户，使用IP地址哈希作为标识
                userIdentifier = "anonymous:" + IpUtils.hashIpAddress(ipAddress);
                userType = ToolUsageLimit.UserType.ANONYMOUS;
                log.debug("检测到匿名用户，IP: {}", ipAddress);
            }
            
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
            usageLimitService.recordUsage(
                userIdentifier, 
                usageLimit.toolName(), 
                userType,
                userId,
                ipAddress,
                userAgent
            );
            
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