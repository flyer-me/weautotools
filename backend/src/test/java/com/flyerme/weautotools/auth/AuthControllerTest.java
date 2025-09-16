package com.flyerme.weautotools.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flyerme.weautotools.dto.AuthenticatedUser;
import com.flyerme.weautotools.dto.TokenResponse;
import com.flyerme.weautotools.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private AuthenticationCenterService authenticationCenterService;

    @Test
    void testAuthenticateUser() throws Exception {
        LoginRequest loginRequest = new LoginRequest("testuser", "password");
        
        // 创建测试用户
        User testUser = new User();
        testUser.setId(1L);
        testUser.setMobile("testuser");
        testUser.setRoles(List.of("USER"));

        Authentication authentication = new UsernamePasswordAuthenticationToken(testUser, "password");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        
        // 创建预期Token响应
        AuthenticatedUser authenticatedUser = AuthenticatedUser.builder()
                .userId(1L)
                .username("testuser")
                .userType("USER")
                .authorities(List.of("USER"))
                .build();
        
        TokenResponse tokenResponse = TokenResponse.success("test-jwt-token", 900L, authenticatedUser);
        when(authenticationCenterService.generateTokenResponse(any(Authentication.class))).thenReturn(tokenResponse);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("test-jwt-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.userId").value(1))
                .andExpect(jsonPath("$.user.username").value("testuser"));
    }

    @Test
    void testLogoutUser() throws Exception {
        mockMvc.perform(post("/auth/logout")
                        .header("Authorization", "Bearer test-jwt-token"))
                .andExpect(status().isOk());
    }
}
