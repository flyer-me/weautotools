package com.flyerme.weautotools.auth;

import com.flyerme.weautotools.dto.AuthenticatedUser;
import com.flyerme.weautotools.dto.TokenResponse;
import com.flyerme.weautotools.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * AuthenticationCenterService 单元测试
 * 移动到auth包中，与认证逻辑保持一致
 *
 * @author WeAutoTools Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@ExtendWith(MockitoExtension.class)
class AuthenticationCenterServiceTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private JwtTokenBlacklist jwtTokenBlacklist;

    @Mock
    private HttpServletRequest request;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthenticationCenterService authenticationCenterService;

    private User testUser;
    private String testToken;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setMobile("test@example.com");
        testUser.setRoles(List.of("USER"));
        
        testToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.signature";
        
        // 设置请求mock
        when(request.getHeader("User-Agent")).thenReturn("TestAgent");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
    }

    @Test
    void testValidateAndExtractUser_WithValidToken() {
        // 准备数据
        when(jwtTokenProvider.extractTokenFromRequest(request)).thenReturn(testToken);
        when(jwtTokenBlacklist.isBlacklisted(testToken)).thenReturn(false);
        when(jwtTokenProvider.validateToken(testToken)).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromJWT(testToken)).thenReturn("test@example.com");
        when(jwtTokenProvider.getUserIdFromJWT(testToken)).thenReturn(1L);

        // 执行测试
        AuthenticatedUser result = authenticationCenterService.validateAndExtractUser(request);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isAuthenticated());
        assertEquals(1L, result.getUserId());
        assertEquals("test@example.com", result.getUsername());
        assertEquals("USER", result.getUserType());
    }

    @Test
    void testValidateAndExtractUser_WithInvalidToken() {
        // 准备数据
        when(jwtTokenProvider.extractTokenFromRequest(request)).thenReturn(testToken);
        when(jwtTokenProvider.validateToken(testToken)).thenReturn(false);

        // 执行测试
        AuthenticatedUser result = authenticationCenterService.validateAndExtractUser(request);

        // 验证结果
        assertNotNull(result);
        assertFalse(result.isAuthenticated());
        assertEquals("ANONYMOUS", result.getUserType());
    }

    @Test
    void testValidateAndExtractUser_WithBlacklistedToken() {
        // 准备数据
        when(jwtTokenProvider.extractTokenFromRequest(request)).thenReturn(testToken);
        when(jwtTokenBlacklist.isBlacklisted(testToken)).thenReturn(true);

        // 执行测试
        AuthenticatedUser result = authenticationCenterService.validateAndExtractUser(request);

        // 验证结果
        assertNotNull(result);
        assertFalse(result.isAuthenticated());
        assertEquals("ANONYMOUS", result.getUserType());
    }

    @Test
    void testValidateAndExtractUser_WithNoToken() {
        // 准备数据
        when(jwtTokenProvider.extractTokenFromRequest(request)).thenReturn(null);

        // 执行测试
        AuthenticatedUser result = authenticationCenterService.validateAndExtractUser(request);

        // 验证结果
        assertNotNull(result);
        assertFalse(result.isAuthenticated());
        assertEquals("ANONYMOUS", result.getUserType());
    }

    @Test
    void testExtractUserFromToken_Success() {
        // 准备数据
        when(jwtTokenProvider.getUsernameFromJWT(testToken)).thenReturn("test@example.com");
        when(jwtTokenProvider.getUserIdFromJWT(testToken)).thenReturn(1L);

        // 执行测试
        AuthenticatedUser result = authenticationCenterService.extractUserFromToken(testToken);

        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals("test@example.com", result.getUsername());
        assertEquals("USER", result.getUserType());
    }

    @Test
    void testExtractUserFromToken_InvalidToken() {
        // 准备数据
        when(jwtTokenProvider.getUsernameFromJWT(testToken)).thenReturn(null);

        // 执行测试
        AuthenticatedUser result = authenticationCenterService.extractUserFromToken(testToken);

        // 验证结果
        assertNull(result);
    }

    @Test
    void testGenerateTokenResponse_Success() {
        // 准备数据
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(jwtTokenProvider.generateToken(authentication)).thenReturn(testToken);

        // 执行测试
        TokenResponse result = authenticationCenterService.generateTokenResponse(authentication);

        // 验证结果
        assertNotNull(result);
        assertEquals(testToken, result.getAccessToken());
        assertEquals("Bearer", result.getTokenType());
        assertNotNull(result.getUser());
        assertEquals(1L, result.getUser().getUserId());
    }

    @Test
    void testInvalidateToken_Success() {
        // 准备数据
        Date expiration = new Date(System.currentTimeMillis() + 86400000);
        when(jwtTokenProvider.validateToken(testToken)).thenReturn(true);
        when(jwtTokenProvider.getExpirationDateFromToken(testToken)).thenReturn(expiration);

        // 执行测试
        authenticationCenterService.invalidateToken(testToken);

        // 验证调用
        verify(jwtTokenBlacklist).add(testToken, expiration);
    }

    @Test
    void testInvalidateToken_InvalidToken() {
        // 准备数据
        when(jwtTokenProvider.validateToken(testToken)).thenReturn(false);

        // 执行测试
        authenticationCenterService.invalidateToken(testToken);

        // 验证没有调用黑名单添加
        verify(jwtTokenBlacklist, never()).add(anyString(), any(Date.class));
    }

    @Test
    void testGetCurrentUser_FromRequest() {
        // 准备数据
        AuthenticatedUser testUser = AuthenticatedUser.builder()
                .userId(1L)
                .username("test@example.com")
                .userType("USER")
                .build();
        when(request.getAttribute("AUTHENTICATED_USER")).thenReturn(testUser);

        // 执行测试
        AuthenticatedUser result = authenticationCenterService.getCurrentUser(request);

        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals("test@example.com", result.getUsername());
    }

    @Test
    void testSetUserToRequest() {
        // 准备数据
        AuthenticatedUser user = AuthenticatedUser.builder()
                .userId(1L)
                .username("test@example.com")
                .userType("USER")
                .authorities(List.of("USER"))
                .clientIp("127.0.0.1")
                .build();

        // 执行测试
        authenticationCenterService.setUserToRequest(request, user);

        // 验证调用
        verify(request).setAttribute("AUTHENTICATED_USER", user);
        verify(request).setAttribute("USER_ID", 1L);
        verify(request).setAttribute("USER_TYPE", "USER");
        verify(request).setAttribute("USER_AUTHORITIES", List.of("USER"));
        verify(request).setAttribute("CLIENT_IP", "127.0.0.1");
        verify(request).setAttribute(eq("REQUEST_TIMESTAMP"), any(Long.class));
    }
}