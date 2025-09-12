package com.flyerme.weautotools.auth;

import com.flyerme.weautotools.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    @InjectMocks
    private JwtTokenProvider jwtTokenProvider;

    private final String testSecret = "ThisIsASecretKeyForJwtGenerationWhichIsLongEnoughAndSecure";
    private final long testExpiration = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", testSecret);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationInMs", testExpiration);
    }

    private Authentication createTestAuthentication() {
        User user = new User();
        user.setId(123L);
        user.setMobile("1234567890");
        user.setRoles(List.of("ROLE_USER", "ROLE_ADMIN"));
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    @Test
    void testGenerateToken() {
        Authentication authentication = createTestAuthentication();
        String token = jwtTokenProvider.generateToken(authentication);

        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    void testGetUsernameFromJWT() {
        Authentication authentication = createTestAuthentication();
        String token = jwtTokenProvider.generateToken(authentication);
        String username = jwtTokenProvider.getUsernameFromJWT(token);

        assertEquals("1234567890", username);
    }

    @Test
    void testGetUserIdFromJWT() {
        Authentication authentication = createTestAuthentication();
        String token = jwtTokenProvider.generateToken(authentication);
        Long userId = jwtTokenProvider.getUserIdFromJWT(token);

        assertEquals(123L, userId);
    }

    @Test
    void testGetUserIdFromJWT_InvalidToken() {
        String invalidToken = "invalid-token";
        Long userId = jwtTokenProvider.getUserIdFromJWT(invalidToken);
        
        assertNull(userId);
    }

    @Test
    void testValidateToken() {
        Authentication authentication = createTestAuthentication();
        String token = jwtTokenProvider.generateToken(authentication);

        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    void testValidateToken_InvalidToken() {
        String invalidToken = "invalid-token";
        assertFalse(jwtTokenProvider.validateToken(invalidToken));
    }

    @Test
    void testValidateToken_ExpiredToken() throws InterruptedException {
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationInMs", 1L); // 1 ms
        Authentication authentication = createTestAuthentication();
        String token = jwtTokenProvider.generateToken(authentication);
        
        Thread.sleep(5); // Wait for token to expire

        assertFalse(jwtTokenProvider.validateToken(token));
    }
}
