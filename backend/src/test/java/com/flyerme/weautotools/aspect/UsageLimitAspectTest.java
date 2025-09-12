import com.flyerme.weautotools.annotation.UsageLimit;
import com.flyerme.weautotools.aspect.UsageLimitAspect;
import com.flyerme.weautotools.auth.JwtTokenProvider;
import com.flyerme.weautotools.common.Result;
import com.flyerme.weautotools.entity.ToolUsageLimit;
import com.flyerme.weautotools.service.UsageLimitService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 使用限制切面单元测试
 *
 * @author WeAutoTools Team
 * @version 1.0.2
 * @since 2025-09-12
 */
@ExtendWith(MockitoExtension.class)
class UsageLimitAspectTest {

    @Mock
    private UsageLimitService usageLimitService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private HttpServletRequest request;

    @Mock
    private ServletRequestAttributes requestAttributes;

    @InjectMocks
    private UsageLimitAspect usageLimitAspect;

    private UsageLimit usageLimit;

    @BeforeEach
    void setUp() {
        usageLimit = createUsageLimit("qr-generate", "QR_CODE", false, "测试限制");
        
        // 设置请求模拟
        when(requestAttributes.getRequest()).thenReturn(request);
        when(request.getHeader("User-Agent")).thenReturn("Test Agent");
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        // 添加IpUtils用到的其他header
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(request.getHeader("X-Forwarded")).thenReturn(null);
        when(request.getHeader("Forwarded-For")).thenReturn(null);
        when(request.getHeader("Forwarded")).thenReturn(null);
    }

    @Test
    void testCheckUsageLimit_AnonymousUser_NotExceeded() throws Throwable {
        // 安排
        when(request.getHeader("Authorization")).thenReturn(null);
        when(usageLimitService.isExceededLimit(anyString(), eq("qr-generate"), eq(ToolUsageLimit.UserType.ANONYMOUS)))
            .thenReturn(false);
        when(joinPoint.proceed()).thenReturn("success");

        try (MockedStatic<RequestContextHolder> mockedStatic = mockStatic(RequestContextHolder.class)) {
            mockedStatic.when(RequestContextHolder::getRequestAttributes).thenReturn(requestAttributes);

            // 执行
            Object result = usageLimitAspect.checkUsageLimit(joinPoint, usageLimit);

            // 验证
            assertEquals("success", result);
            verify(usageLimitService).recordUsage(anyString(), eq("qr-generate"), 
                eq(ToolUsageLimit.UserType.ANONYMOUS), isNull(), anyString(), anyString());
            verify(joinPoint).proceed();
        }
    }

    @Test
    void testCheckUsageLimit_AnonymousUser_Exceeded() throws Throwable {
        // 安排
        when(request.getHeader("Authorization")).thenReturn(null);
        when(usageLimitService.isExceededLimit(anyString(), eq("qr-generate"), eq(ToolUsageLimit.UserType.ANONYMOUS)))
            .thenReturn(true);

        try (MockedStatic<RequestContextHolder> mockedStatic = mockStatic(RequestContextHolder.class)) {
            mockedStatic.when(RequestContextHolder::getRequestAttributes).thenReturn(requestAttributes);

            // 执行
            Object result = usageLimitAspect.checkUsageLimit(joinPoint, usageLimit);

            // 验证
            assertInstanceOf(Result.class, result);
            Result<?> resultObj = (Result<?>) result;
            assertEquals(429, resultObj.getCode());
            assertTrue(resultObj.getMessage().contains("测试限制"));
            
            verify(usageLimitService, never()).recordUsage(anyString(), anyString(), any(), any(), anyString(), anyString());
            verify(joinPoint, never()).proceed();
        }
    }

    @Test
    void testCheckUsageLimit_LoginUser_WithValidToken() throws Throwable {
        // 安排
        String validToken = "valid.jwt.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        
        // 模拟JWT验证和用户ID提取
        try (MockedStatic<JwtTokenProvider> jwtMockedStatic = 
                mockStatic(JwtTokenProvider.class);
             MockedStatic<RequestContextHolder> contextMockedStatic = 
                mockStatic(RequestContextHolder.class)) {
            
            jwtMockedStatic.when(() -> JwtTokenProvider.extractTokenFromRequest(request))
                .thenReturn(validToken);
            when(jwtTokenProvider.validateToken(validToken))
                .thenReturn(true);
            when(jwtTokenProvider.getUserIdFromJWT(validToken))
                .thenReturn(123L);
            contextMockedStatic.when(RequestContextHolder::getRequestAttributes).thenReturn(requestAttributes);

            when(usageLimitService.isExceededLimit(eq("user:123"), eq("qr-generate"), eq(ToolUsageLimit.UserType.LOGIN)))
                .thenReturn(false);
            when(joinPoint.proceed()).thenReturn("success");

            // 执行
            Object result = usageLimitAspect.checkUsageLimit(joinPoint, usageLimit);

            // 验证
            assertEquals("success", result);
            verify(usageLimitService).recordUsage(eq("user:123"), eq("qr-generate"), 
                eq(ToolUsageLimit.UserType.LOGIN), eq(123L), anyString(), anyString());
            verify(joinPoint).proceed();
        }
    }

    @Test
    void testCheckUsageLimit_LoginUser_WithInvalidToken() throws Throwable {
        // 安排
        String invalidToken = "invalid.jwt.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + invalidToken);

        try (MockedStatic<JwtTokenProvider> jwtMockedStatic = 
                mockStatic(JwtTokenProvider.class);
             MockedStatic<RequestContextHolder> contextMockedStatic = 
                mockStatic(RequestContextHolder.class)) {
            
            jwtMockedStatic.when(() -> JwtTokenProvider.extractTokenFromRequest(request))
                .thenReturn(invalidToken);
            when(jwtTokenProvider.validateToken(invalidToken))
                .thenReturn(false);
            contextMockedStatic.when(RequestContextHolder::getRequestAttributes).thenReturn(requestAttributes);

            when(usageLimitService.isExceededLimit(anyString(), eq("qr-generate"), eq(ToolUsageLimit.UserType.ANONYMOUS)))
                .thenReturn(false);
            when(joinPoint.proceed()).thenReturn("success");

            // 执行
            Object result = usageLimitAspect.checkUsageLimit(joinPoint, usageLimit);

            // 验证
            assertEquals("success", result);
            verify(usageLimitService).recordUsage(anyString(), eq("qr-generate"), 
                eq(ToolUsageLimit.UserType.ANONYMOUS), isNull(), anyString(), anyString());
        }
    }

    @Test
    void testCheckUsageLimit_NoRequestContext() throws Throwable {
        // 安排
        try (MockedStatic<RequestContextHolder> mockedStatic = mockStatic(RequestContextHolder.class)) {
            mockedStatic.when(RequestContextHolder::getRequestAttributes).thenReturn(null);
            when(joinPoint.proceed()).thenReturn("success");

            // 执行
            Object result = usageLimitAspect.checkUsageLimit(joinPoint, usageLimit);

            // 验证
            assertEquals("success", result);
            verify(usageLimitService, never()).isExceededLimit(anyString(), anyString(), any());
            verify(joinPoint).proceed();
        }
    }

    @Test
    void testCheckUsageLimit_ExceptionHandling() throws Throwable {
        // 安排
        try (MockedStatic<RequestContextHolder> mockedStatic = mockStatic(RequestContextHolder.class)) {
            mockedStatic.when(RequestContextHolder::getRequestAttributes).thenReturn(requestAttributes);
            when(usageLimitService.isExceededLimit(anyString(), anyString(), any()))
                .thenThrow(new RuntimeException("Test exception"));
            when(joinPoint.proceed()).thenReturn("success");

            // 执行
            Object result = usageLimitAspect.checkUsageLimit(joinPoint, usageLimit);

            // 验证
            assertEquals("success", result);
            verify(joinPoint).proceed(); // 异常情况下应该继续执行
        }
    }

    /**
     * 创建使用限制注解的辅助方法
     */
    private UsageLimit createUsageLimit(String toolName, String toolType, boolean requireAuth, String message) {
        return new UsageLimit() {
            @Override
            public String toolName() { return toolName; }
            
            @Override
            public String toolType() { return toolType; }
            
            @Override
            public boolean requireAuth() { return requireAuth; }
            
            @Override
            public String message() { return message; }
            
            @Override
            public Class<UsageLimit> annotationType() { return UsageLimit.class; }
        };
    }
}