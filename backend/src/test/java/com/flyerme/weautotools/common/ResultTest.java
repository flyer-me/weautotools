package com.flyerme.weautotools.controller;

import com.flyerme.weautotools.common.Result;
import com.flyerme.weautotools.common.ResultCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Result 响应格式测试类
 *
 * @author WeAutoTools Team
 * @version 1.0.0
 * @since 2025-01-20
 */
class ResultTest {

    @Test
    void testSuccessResponse() {
        Result<String> result = Result.success("测试数据");
        
        assertTrue(result.isSuccess());
        assertEquals(ResultCode.SUCCESS.getCode(), result.getCode());
        assertEquals(ResultCode.SUCCESS.getMessage(), result.getMessage());
        assertEquals("测试数据", result.getData());
    }

    @Test
    void testSuccessResponseWithMessage() {
        Result<String> result = Result.success("操作成功", "测试数据");
        
        assertTrue(result.isSuccess());
        assertEquals(ResultCode.SUCCESS.getCode(), result.getCode());
        assertEquals("操作成功", result.getMessage());
        assertEquals("测试数据", result.getData());
    }

    @Test
    void testErrorResponse() {
        Result<Void> result = Result.error("操作失败");
        
        assertFalse(result.isSuccess());
        assertEquals(ResultCode.INTERNAL_SERVER_ERROR.getCode(), result.getCode());
        assertEquals("操作失败", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    void testErrorResponseWithCode() {
        Result<Void> result = Result.error(4001, "认证失败");
        
        assertFalse(result.isSuccess());
        assertEquals(4001, result.getCode());
        assertEquals("认证失败", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    void testErrorResponseWithResultCode() {
        Result<Void> result = Result.error(ResultCode.AUTH_FAILED);
        
        assertFalse(result.isSuccess());
        assertEquals(ResultCode.AUTH_FAILED.getCode(), result.getCode());
        assertEquals(ResultCode.AUTH_FAILED.getMessage(), result.getMessage());
        assertNull(result.getData());
    }

    @Test
    void testOfMethod() {
        Result<String> result = Result.of(ResultCode.SUCCESS, "测试数据");
        
        assertTrue(result.isSuccess());
        assertEquals(ResultCode.SUCCESS.getCode(), result.getCode());
        assertEquals(ResultCode.SUCCESS.getMessage(), result.getMessage());
        assertEquals("测试数据", result.getData());
    }
}