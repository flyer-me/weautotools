package com.flyerme.weautotools.service.impl;

import com.flyerme.weautotools.dto.ClickCounterRequest;
import com.flyerme.weautotools.dto.ClickCounterResponse;
import com.flyerme.weautotools.entity.ClickCounter;
import com.flyerme.weautotools.exception.BusinessException;
import com.flyerme.weautotools.mapper.ClickCounterMapper;
import com.flyerme.weautotools.service.ClickCounterService.ClickCounterStatistics;
import com.flyerme.weautotools.util.DistributedLockUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ClickCounterService实现类测试
 *
 * @author WeAutoTools Team
 * @version 1.0.0
 * @since 2025-08-17
 */
@ExtendWith(MockitoExtension.class)
class ClickCounterServiceImplTest {

    @Mock
    private ClickCounterMapper clickCounterMapper;

    @Mock
    private DistributedLockUtil distributedLockUtil;

    @InjectMocks
    private ClickCounterServiceImpl clickCounterService;

    private ClickCounter testCounter;
    private ClickCounterRequest testRequest;

    @BeforeEach
    void setUp() {
        // 设置测试数据
        testCounter = new ClickCounter();
        testCounter.setId(1L);
        testCounter.setCounterName("test_counter");
        testCounter.setDescription("Test Counter");
        testCounter.setClickCount(10L);
        testCounter.setEnabled(true);
        testCounter.setCreatedAt(LocalDateTime.now());
        testCounter.setUpdatedAt(LocalDateTime.now());
        testCounter.setDeleted(0);
        testCounter.setVersion(0);

        testRequest = new ClickCounterRequest();
        testRequest.setCounterName("test_counter");
        testRequest.setDescription("Test Counter");
        testRequest.setEnabled(true);

        // Mock分布式锁工具
        when(distributedLockUtil.executeWithLock(anyString(), any(Supplier.class)))
                .thenAnswer(invocation -> {
                    Supplier<?> supplier = invocation.getArgument(1);
                    return supplier.get();
                });
    }

    @Test
    void testCreateCounter_Success() {
        // Given
        when(clickCounterMapper.selectByName(testRequest.getCounterName())).thenReturn(null);
        when(clickCounterMapper.insert(any(ClickCounter.class))).thenReturn(1);

        // When
        ClickCounterResponse response = clickCounterService.createCounter(testRequest);

        // Then
        assertNotNull(response);
        assertEquals(testRequest.getCounterName(), response.getCounterName());
        assertEquals(testRequest.getDescription(), response.getDescription());
        assertEquals(0L, response.getClickCount());
        verify(clickCounterMapper).selectByName(testRequest.getCounterName());
        verify(clickCounterMapper).insert(any(ClickCounter.class));
    }

    @Test
    void testCreateCounter_NameAlreadyExists() {
        // Given
        when(clickCounterMapper.selectByName(testRequest.getCounterName())).thenReturn(testCounter);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            clickCounterService.createCounter(testRequest);
        });
        assertEquals("计数器名称已存在: " + testRequest.getCounterName(), exception.getMessage());
    }

    @Test
    void testGetCounterById_Success() {
        // Given
        when(clickCounterMapper.selectById(1L)).thenReturn(testCounter);

        // When
        ClickCounterResponse response = clickCounterService.getCounterById(1L);

        // Then
        assertNotNull(response);
        assertEquals(testCounter.getId(), response.getId());
        assertEquals(testCounter.getCounterName(), response.getCounterName());
    }

    @Test
    void testGetCounterById_NotFound() {
        // Given
        when(clickCounterMapper.selectById(1L)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            clickCounterService.getCounterById(1L);
        });
        assertEquals("计数器不存在: 1", exception.getMessage());
    }

    @Test
    void testGetCounterByName_Success() {
        // Given
        when(clickCounterMapper.selectByName("test_counter")).thenReturn(testCounter);

        // When
        ClickCounterResponse response = clickCounterService.getCounterByName("test_counter");

        // Then
        assertNotNull(response);
        assertEquals(testCounter.getCounterName(), response.getCounterName());
    }

    @Test
    void testUpdateCounter_Success() {
        // Given
        when(clickCounterMapper.selectById(1L)).thenReturn(testCounter);
        when(clickCounterMapper.countByNameExcludeId(anyString(), anyLong())).thenReturn(0L);
        when(clickCounterMapper.update(any(ClickCounter.class))).thenReturn(1);

        ClickCounterRequest updateRequest = new ClickCounterRequest();
        updateRequest.setCounterName("updated_counter");
        updateRequest.setDescription("Updated Counter");
        updateRequest.setEnabled(false);

        // When
        ClickCounterResponse response = clickCounterService.updateCounter(1L, updateRequest);

        // Then
        assertNotNull(response);
        verify(clickCounterMapper).update(any(ClickCounter.class));
    }

    @Test
    void testDeleteCounter_Success() {
        // Given
        when(clickCounterMapper.selectById(1L)).thenReturn(testCounter);
        when(clickCounterMapper.deleteById(eq(1L), any(LocalDateTime.class))).thenReturn(1);

        // When & Then
        assertDoesNotThrow(() -> clickCounterService.deleteCounter(1L));
        verify(clickCounterMapper).deleteById(eq(1L), any(LocalDateTime.class));
    }

    @Test
    void testClickCounter_Success() {
        // Given
        when(clickCounterMapper.incrementClickCount(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(1);
        when(clickCounterMapper.selectById(1L)).thenReturn(testCounter);

        // When
        ClickCounterResponse response = clickCounterService.clickCounter(1L);

        // Then
        assertNotNull(response);
        assertEquals(testCounter.getClickCount(), response.getClickCount());
        verify(clickCounterMapper).incrementClickCount(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void testClickCounter_Failed() {
        // Given
        when(clickCounterMapper.incrementClickCount(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(0);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            clickCounterService.clickCounter(1L);
        });
        assertEquals("点击计数失败，计数器可能不存在或已禁用", exception.getMessage());
    }

    @Test
    void testClickCounterByName_Success() {
        // Given
        when(clickCounterMapper.incrementClickCountByName(eq("test_counter"), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(1);
        when(clickCounterMapper.selectByName("test_counter")).thenReturn(testCounter);

        // When
        ClickCounterResponse response = clickCounterService.clickCounterByName("test_counter");

        // Then
        assertNotNull(response);
        assertEquals(testCounter.getClickCount(), response.getClickCount());
    }

    @Test
    void testGetStatistics() {
        // Given
        when(clickCounterMapper.count()).thenReturn(5L);
        when(clickCounterMapper.countEnabled()).thenReturn(3L);
        when(clickCounterMapper.sumTotalClicks()).thenReturn(100L);

        // When
        ClickCounterStatistics statistics = clickCounterService.getStatistics();

        // Then
        assertNotNull(statistics);
        assertEquals(5L, statistics.getTotalCounters());
        assertEquals(3L, statistics.getEnabledCounters());
        assertEquals(100L, statistics.getTotalClicks());
    }

    @Test
    void testGetCountersByPage() {
        // Given
        List<ClickCounter> counters = Arrays.asList(testCounter);
        when(clickCounterMapper.selectByPage(0L, 10)).thenReturn(counters);

        // When
        List<ClickCounterResponse> responses = clickCounterService.getCountersByPage(1, 10);

        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(testCounter.getCounterName(), responses.get(0).getCounterName());
    }

    @Test
    void testGetAllCounters() {
        // Given
        List<ClickCounter> counters = Arrays.asList(testCounter);
        when(clickCounterMapper.selectAll()).thenReturn(counters);

        // When
        List<ClickCounterResponse> responses = clickCounterService.getAllCounters();

        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(testCounter.getCounterName(), responses.get(0).getCounterName());
    }

    @Test
    void testResetCounter_Success() {
        // Given
        when(clickCounterMapper.selectById(1L)).thenReturn(testCounter);
        when(clickCounterMapper.resetClickCount(eq(1L), any(LocalDateTime.class))).thenReturn(1);
        
        ClickCounter resetCounter = new ClickCounter();
        resetCounter.setId(1L);
        resetCounter.setCounterName("test_counter");
        resetCounter.setClickCount(0L);
        when(clickCounterMapper.selectById(1L)).thenReturn(resetCounter);

        // When
        ClickCounterResponse response = clickCounterService.resetCounter(1L);

        // Then
        assertNotNull(response);
        assertEquals(0L, response.getClickCount());
        verify(clickCounterMapper).resetClickCount(eq(1L), any(LocalDateTime.class));
    }

    @Test
    void testGetEnabledCounters() {
        // Given
        List<ClickCounter> counters = Arrays.asList(testCounter);
        when(clickCounterMapper.selectEnabled()).thenReturn(counters);

        // When
        List<ClickCounterResponse> responses = clickCounterService.getEnabledCounters();

        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertTrue(responses.get(0).getEnabled());
    }

    @Test
    void testGetCountersByCondition() {
        // Given
        List<ClickCounter> counters = Arrays.asList(testCounter);
        when(clickCounterMapper.selectByCondition(true, "test")).thenReturn(counters);

        // When
        List<ClickCounterResponse> responses = clickCounterService.getCountersByCondition(true, "test");

        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
    }

    @Test
    void testGetTopCountersByClicks() {
        // Given
        List<ClickCounter> counters = Arrays.asList(testCounter);
        when(clickCounterMapper.selectTopByClicks(5)).thenReturn(counters);

        // When
        List<ClickCounterResponse> responses = clickCounterService.getTopCountersByClicks(5);

        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(testCounter.getClickCount(), responses.get(0).getClickCount());
    }
}
