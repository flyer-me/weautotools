package com.flyerme.weautotools.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flyerme.weautotools.entity.ToolUsageLimit;
import com.flyerme.weautotools.service.UsageLimitService;
import com.flyerme.weautotools.service.UsageLimitConfigService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 使用限制控制器集成测试
 *
 * @author WeAutoTools Team
 * @version 1.0.2
 * @since 2025-09-12
 */
@WebMvcTest(UsageLimitController.class)
class UsageLimitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UsageLimitService usageLimitService;

    @MockBean
    private UsageLimitConfigService usageLimitConfigService;

    @Test
    void testCheckUsageLimit_NotExceeded() throws Exception {
        // 安排
        String toolName = "qr-generate";
        when(usageLimitService.isExceededLimit(anyString(), eq(toolName), any()))
            .thenReturn(false);
        when(usageLimitService.getRemainingUsage(anyString(), eq(toolName), any()))
            .thenReturn(5);

        // 执行 & 验证
        mockMvc.perform(get("/api/usage-limits/check")
                .param("toolName", toolName)
                .header("X-Forwarded-For", "192.168.1.1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.isExceeded").value(false))
                .andExpect(jsonPath("$.data.remaining").value(5))
                .andExpect(jsonPath("$.data.toolName").value(toolName));
    }

    @Test
    void testCheckUsageLimit_Exceeded() throws Exception {
        // 安排
        String toolName = "qr-generate";
        when(usageLimitService.isExceededLimit(anyString(), eq(toolName), any()))
            .thenReturn(true);

        // 执行 & 验证
        mockMvc.perform(get("/api/usage-limits/check")
                .param("toolName", toolName)
                .header("X-Forwarded-For", "192.168.1.1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.isExceeded").value(true))
                .andExpect(jsonPath("$.data.remaining").value(0))
                .andExpect(jsonPath("$.data.toolName").value(toolName));
    }

    @Test
    void testCheckUsageLimit_WithValidJWT() throws Exception {
        // 安排
        String toolName = "qr-generate";
        String validToken = "valid.jwt.token";
        
        when(usageLimitService.isExceededLimit(anyString(), eq(toolName), eq(ToolUsageLimit.UserType.LOGIN)))
            .thenReturn(false);
        when(usageLimitService.getRemainingUsage(anyString(), eq(toolName), eq(ToolUsageLimit.UserType.LOGIN)))
            .thenReturn(20);

        // 执行 & 验证
        mockMvc.perform(get("/api/usage-limits/check")
                .param("toolName", toolName)
                .header("Authorization", "Bearer " + validToken)
                .header("X-Forwarded-For", "192.168.1.1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.isExceeded").value(false))
                .andExpect(jsonPath("$.data.remaining").value(20));
    }

    @Test
    void testResetUserUsage() throws Exception {
        // 安排
        String userIdentifier = "anonymous:test123";

        // 执行 & 验证
        mockMvc.perform(post("/api/usage-limits/reset")
                .param("userIdentifier", userIdentifier))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void testCheckUsageLimit_InvalidToolName() throws Exception {
        // 执行 & 验证
        mockMvc.perform(get("/api/usage-limits/check"))
                .andExpect(status().isBadRequest());
    }
}