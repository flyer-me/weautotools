package com.flyerme.weautotools.util;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * IP工具类单元测试
 *
 * @author WeAutoTools Team
 * @version 1.0.2
 * @since 2025-09-12
 */
@ExtendWith(MockitoExtension.class)
class IpUtilsTest {

    @Mock
    private HttpServletRequest request;

    @Test
    void testGetClientIpAddress_XForwardedFor() {
        // 安排
        when(request.getHeader("X-Forwarded-For")).thenReturn("203.0.113.1, 198.51.100.1");
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");

        // 执行
        String ip = IpUtils.getClientIpAddress(request);

        // 验证
        assertEquals("203.0.113.1", ip);
    }

    @Test
    void testGetClientIpAddress_ProxyClientIP() {
        // 安排
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("Proxy-Client-IP")).thenReturn("203.0.113.2");
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");

        // 执行
        String ip = IpUtils.getClientIpAddress(request);

        // 验证
        assertEquals("203.0.113.2", ip);
    }

    @Test
    void testGetClientIpAddress_RemoteAddr() {
        // 安排
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("Proxy-Client-IP")).thenReturn(null);
        when(request.getHeader("WL-Proxy-Client-IP")).thenReturn(null);
        when(request.getHeader("HTTP_X_FORWARDED_FOR")).thenReturn(null);
        when(request.getHeader("HTTP_X_FORWARDED")).thenReturn(null);
        when(request.getHeader("HTTP_X_CLUSTER_CLIENT_IP")).thenReturn(null);
        when(request.getHeader("HTTP_CLIENT_IP")).thenReturn(null);
        when(request.getHeader("HTTP_FORWARDED_FOR")).thenReturn(null);
        when(request.getHeader("HTTP_FORWARDED")).thenReturn(null);
        when(request.getHeader("HTTP_VIA")).thenReturn(null);
        when(request.getHeader("REMOTE_ADDR")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("203.0.113.3");

        // 执行
        String ip = IpUtils.getClientIpAddress(request);

        // 验证
        assertEquals("203.0.113.3", ip);
    }

    @Test
    void testGetClientIpAddress_Unknown() {
        // 安排
        when(request.getHeader("X-Forwarded-For")).thenReturn("unknown");
        when(request.getHeader("Proxy-Client-IP")).thenReturn("");
        when(request.getRemoteAddr()).thenReturn("unknown");

        // 执行
        String ip = IpUtils.getClientIpAddress(request);

        // 验证
        assertEquals("unknown", ip);
    }

    @Test
    void testGetClientIpAddress_LocalAddress() {
        // 安排
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        // 执行
        String ip = IpUtils.getClientIpAddress(request);

        // 验证
        assertEquals("127.0.0.1", ip);
    }

    @Test
    void testGetClientIpAddress_PrivateAddress() {
        // 安排
        when(request.getRemoteAddr()).thenReturn("192.168.1.100");

        // 执行
        String ip = IpUtils.getClientIpAddress(request);

        // 验证
        assertEquals("192.168.1.100", ip);
    }

    @Test
    void testHashIpAddress_SHA256() {
        // 安排
        String ipAddress = "192.168.1.1";

        // 执行
        String hash1 = IpUtils.hashIpAddress(ipAddress);
        String hash2 = IpUtils.hashIpAddress(ipAddress);

        // 验证
        assertNotNull(hash1);
        assertEquals(16, hash1.length()); // 前16位
        assertEquals(hash1, hash2); // 相同IP应该生成相同哈希
        assertTrue(hash1.matches("[a-f0-9]+"));
    }

    @Test
    void testHashIpAddress_DifferentIPs() {
        // 安排
        String ip1 = "192.168.1.1";
        String ip2 = "192.168.1.2";

        // 执行
        String hash1 = IpUtils.hashIpAddress(ip1);
        String hash2 = IpUtils.hashIpAddress(ip2);

        // 验证
        assertNotEquals(hash1, hash2); // 不同IP应该生成不同哈希
    }

    @Test
    void testHashIpAddress_IPv6() {
        // 安排
        String ipv6 = "2001:0db8:85a3:0000:0000:8a2e:0370:7334";

        // 执行
        String hash = IpUtils.hashIpAddress(ipv6);

        // 验证
        assertNotNull(hash);
        assertEquals(16, hash.length());
        assertTrue(hash.matches("[a-f0-9]+"));
    }
}