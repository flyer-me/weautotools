package com.flyerme.weautotools.service;

import com.flyerme.weautotools.entity.Tool;
import com.flyerme.weautotools.mapper.ToolMapper;
import com.flyerme.weautotools.service.impl.ToolServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 工具服务测试类
 *
 * @author WeAutoTools Team
 * @version 1.0.3
 * @since 2025-09-16
 */
@ExtendWith(MockitoExtension.class)
class ToolServiceTest {

    @Mock
    private ToolMapper toolMapper;

    @InjectMocks
    private ToolServiceImpl toolService;

    private Tool testTool;

    @BeforeEach
    void setUp() {
        testTool = new Tool();
        testTool.setId(1L);
        testTool.setToolCode("test-tool");
        testTool.setToolName("测试工具");
        testTool.setToolType("QR_CODE");
        testTool.setDescription("这是一个测试工具");
        testTool.setCategory("qrcode");
        testTool.setStatus("ACTIVE");
        testTool.setIsFrontend(false);
    }

    @Test
    void testGetByToolCode_Success() {
        // Given
        when(toolMapper.selectByToolCode("test-tool")).thenReturn(testTool);

        // When
        Optional<Tool> result = toolService.getByToolCode("test-tool");

        // Then
        assertTrue(result.isPresent());
        assertEquals("test-tool", result.get().getToolCode());
        assertEquals("测试工具", result.get().getToolName());
        verify(toolMapper).selectByToolCode("test-tool");
    }

    @Test
    void testGetByToolCode_NotFound() {
        // Given
        when(toolMapper.selectByToolCode("non-existent")).thenReturn(null);

        // When
        Optional<Tool> result = toolService.getByToolCode("non-existent");

        // Then
        assertFalse(result.isPresent());
        verify(toolMapper).selectByToolCode("non-existent");
    }

    @Test
    void testGetByToolType() {
        // Given
        List<Tool> tools = Arrays.asList(testTool);
        when(toolMapper.selectByToolType("QR_CODE")).thenReturn(tools);

        // When
        List<Tool> result = toolService.getByToolType("QR_CODE");

        // Then
        assertEquals(1, result.size());
        assertEquals("test-tool", result.get(0).getToolCode());
        verify(toolMapper).selectByToolType("QR_CODE");
    }

    @Test
    void testGetActiveTools() {
        // Given
        List<Tool> tools = Arrays.asList(testTool);
        when(toolMapper.selectActiveTools()).thenReturn(tools);

        // When
        List<Tool> result = toolService.getActiveTools();

        // Then
        assertEquals(1, result.size());
        assertTrue(result.get(0).isActive());
        verify(toolMapper).selectActiveTools();
    }

    @Test
    void testCreateTool_Success() {
        // Given
        Tool newTool = new Tool();
        newTool.setToolCode("new-tool");
        newTool.setToolName("新工具");
        newTool.setToolType("IMAGE_PROCESS");
        
        when(toolMapper.existsByToolCode("new-tool")).thenReturn(0);
        when(toolMapper.insert(any(Tool.class))).thenReturn(1);

        // When
        Tool result = toolService.createTool(newTool);

        // Then
        assertNotNull(result);
        assertEquals("new-tool", result.getToolCode());
        verify(toolMapper).existsByToolCode("new-tool");
        verify(toolMapper).insert(newTool);
    }

    @Test
    void testCreateTool_DuplicateCode() {
        // Given
        Tool newTool = new Tool();
        newTool.setToolCode("existing-tool");
        
        when(toolMapper.existsByToolCode("existing-tool")).thenReturn(1);

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> toolService.createTool(newTool)
        );
        
        assertTrue(exception.getMessage().contains("工具代码已存在"));
        verify(toolMapper).existsByToolCode("existing-tool");
        verify(toolMapper, never()).insert(any(Tool.class));
    }

    @Test
    void testUpdateTool() {
        // Given
        when(toolMapper.updateById(any(Tool.class))).thenReturn(1);

        // When
        Tool result = toolService.updateTool(testTool);

        // Then
        assertNotNull(result);
        assertEquals(testTool.getToolCode(), result.getToolCode());
        verify(toolMapper).updateById(testTool);
    }

    @Test
    void testDeleteTool_Success() {
        // Given
        when(toolMapper.selectById(1L)).thenReturn(testTool);
        when(toolMapper.updateById(any(Tool.class))).thenReturn(1);

        // When
        boolean result = toolService.deleteTool(1L);

        // Then
        assertTrue(result);
        verify(toolMapper).selectById(1L);
        verify(toolMapper).updateById(any(Tool.class));
    }

    @Test
    void testDeleteTool_NotFound() {
        // Given
        when(toolMapper.selectById(999L)).thenReturn(null);

        // When
        boolean result = toolService.deleteTool(999L);

        // Then
        assertFalse(result);
        verify(toolMapper).selectById(999L);
        verify(toolMapper, never()).updateById(any(Tool.class));
    }

    @Test
    void testGetToolIdByCode() {
        // Given
        when(toolMapper.selectByToolCode("test-tool")).thenReturn(testTool);

        // When
        Long result = toolService.getToolIdByCode("test-tool");

        // Then
        assertEquals(1L, result);
        verify(toolMapper).selectByToolCode("test-tool");
    }

    @Test
    void testGetToolIdByCode_NotFound() {
        // Given
        when(toolMapper.selectByToolCode("non-existent")).thenReturn(null);

        // When
        Long result = toolService.getToolIdByCode("non-existent");

        // Then
        assertNull(result);
        verify(toolMapper).selectByToolCode("non-existent");
    }

    @Test
    void testExistsByToolCode() {
        // Given
        when(toolMapper.existsByToolCode("test-tool")).thenReturn(1);

        // When
        boolean result = toolService.existsByToolCode("test-tool");

        // Then
        assertTrue(result);
        verify(toolMapper).existsByToolCode("test-tool");
    }
}