package com.flyerme.weautotools.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;

import java.time.Duration;
import java.util.Date;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class JwtTokenBlacklistTest {

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private RBucket<String> rBucket;

    private JwtTokenBlacklist jwtTokenBlacklist;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        jwtTokenBlacklist = new JwtTokenBlacklist(redissonClient);
        doReturn(rBucket).when(redissonClient).getBucket(anyString());
    }

    @Test
    void testAdd() {
        String token = "test-token";
        Date expiration = new Date(System.currentTimeMillis() + 10000); // 10 seconds from now

        jwtTokenBlacklist.add(token, expiration);

        verify(rBucket).set(eq("blacklisted"), any(Duration.class));
    }

    @Test
    void testAdd_NullToken() {
        jwtTokenBlacklist.add(null, new Date());
        verify(redissonClient, never()).getBucket(anyString());
    }

    @Test
    void testAdd_ExpiredToken() {
        String token = "test-token";
        Date expiration = new Date(System.currentTimeMillis() - 10000); // 10 seconds ago

        jwtTokenBlacklist.add(token, expiration);
        verify(redissonClient, never()).getBucket(anyString());
    }

    @Test
    void testIsBlacklisted() {
        when(rBucket.isExists()).thenReturn(true);
        assertTrue(jwtTokenBlacklist.isBlacklisted("some-token"));
    }

    @Test
    void testIsNotBlacklisted() {
        when(rBucket.isExists()).thenReturn(false);
        assertFalse(jwtTokenBlacklist.isBlacklisted("some-token"));
    }
}
