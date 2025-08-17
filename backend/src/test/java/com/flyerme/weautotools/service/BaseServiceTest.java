package com.flyerme.weautotools.service;

import com.flyerme.weautotools.common.BaseEntity;
import com.flyerme.weautotools.exception.BusinessException;
import com.flyerme.weautotools.util.DistributedLockUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

/**
 * BaseService测试类
 *
 * @author WeAutoTools Team
 * @version 1.0.0
 * @since 2025-08-17
 */
@ExtendWith(MockitoExtension.class)
class BaseServiceTest {

    @Mock
    private DistributedLockUtil distributedLockUtil;

    private TestService testService;

    // 测试实体类
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class TestEntity extends BaseEntity {
        private String name;
        private String description;
    }

    // 测试请求DTO
    @Data
    public static class TestRequest {
        private String name;
        private String description;
    }

    // 测试响应DTO
    @Data
    public static class TestResponse {
        private Long id;
        private String name;
        private String description;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    // 测试Service实现
    public static class TestService extends BaseService<TestEntity, TestRequest, TestResponse> {
        
        private TestEntity mockEntity;
        private int insertResult = 1;
        private int updateResult = 1;
        private int deleteResult = 1;

        @Override
        protected Class<TestEntity> getEntityClass() {
            return TestEntity.class;
        }

        @Override
        protected Class<TestResponse> getResponseClass() {
            return TestResponse.class;
        }

        @Override
        protected String getEntityName() {
            return "测试实体";
        }

        @Override
        protected TestEntity selectById(Long id) {
            return mockEntity;
        }

        @Override
        protected int insert(TestEntity entity) {
            if (entity != null) {
                entity.setId(1L);
            }
            return insertResult;
        }

        @Override
        protected int update(TestEntity entity) {
            return updateResult;
        }

        @Override
        protected int deleteById(Long id, LocalDateTime deleteTime) {
            return deleteResult;
        }

        @Override
        protected List<TestEntity> selectAll() {
            return Arrays.asList(mockEntity);
        }

        // 测试用的setter方法
        public void setMockEntity(TestEntity entity) {
            this.mockEntity = entity;
        }

        public void setInsertResult(int result) {
            this.insertResult = result;
        }

        public void setUpdateResult(int result) {
            this.updateResult = result;
        }

        public void setDeleteResult(int result) {
            this.deleteResult = result;
        }
    }

    @BeforeEach
    void setUp() {
        testService = new TestService();
        testService.distributedLockUtil = distributedLockUtil;
    }

    private void mockDistributedLock() {
        // Mock分布式锁工具 - 有返回值的方法
        lenient().when(distributedLockUtil.executeWithLock(anyString(), any(Supplier.class)))
                .thenAnswer(invocation -> {
                    Supplier<?> supplier = invocation.getArgument(1);
                    return supplier.get();
                });

        // Mock分布式锁工具 - 无返回值的方法
        lenient().doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(1);
            runnable.run();
            return null;
        }).when(distributedLockUtil).executeWithLock(anyString(), any(Runnable.class));
    }

    @Test
    void testCreate_Success() {
        // Given
        mockDistributedLock();
        TestRequest request = new TestRequest();
        request.setName("Test Name");
        request.setDescription("Test Description");

        // When
        TestResponse response = testService.create(request);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Test Name", response.getName());
        assertEquals("Test Description", response.getDescription());
        assertNotNull(response.getCreatedAt());
        assertNotNull(response.getUpdatedAt());
    }

    @Test
    void testCreate_InsertFailed() {
        // Given
        mockDistributedLock();
        TestRequest request = new TestRequest();
        request.setName("Test Name");
        testService.setInsertResult(0);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            testService.create(request);
        });
        assertEquals("创建测试实体失败", exception.getMessage());
    }

    @Test
    void testGetById_Success() {
        // Given
        TestEntity entity = new TestEntity();
        entity.setId(1L);
        entity.setName("Test Name");
        testService.setMockEntity(entity);

        // When
        TestResponse response = testService.getById(1L);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Test Name", response.getName());
    }

    @Test
    void testGetById_NotFound() {
        // Given
        testService.setMockEntity(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            testService.getById(1L);
        });
        assertEquals("测试实体不存在: 1", exception.getMessage());
    }

    @Test
    void testUpdate_Success() {
        // Given
        mockDistributedLock();
        TestEntity entity = new TestEntity();
        entity.setId(1L);
        entity.setName("Old Name");
        testService.setMockEntity(entity);

        TestRequest request = new TestRequest();
        request.setName("New Name");
        request.setDescription("New Description");

        // When
        TestResponse response = testService.update(1L, request);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("New Name", response.getName());
        assertEquals("New Description", response.getDescription());
    }

    @Test
    void testUpdate_NotFound() {
        // Given
        mockDistributedLock();
        testService.setMockEntity(null);
        TestRequest request = new TestRequest();

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            testService.update(1L, request);
        });
        assertEquals("测试实体不存在: 1", exception.getMessage());
    }

    @Test
    void testUpdate_UpdateFailed() {
        // Given
        mockDistributedLock();
        TestEntity entity = new TestEntity();
        entity.setId(1L);
        testService.setMockEntity(entity);
        testService.setUpdateResult(0);

        TestRequest request = new TestRequest();

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            testService.update(1L, request);
        });
        assertEquals("更新测试实体失败", exception.getMessage());
    }

    @Test
    void testDelete_Success() {
        // Given
        mockDistributedLock();
        TestEntity entity = new TestEntity();
        entity.setId(1L);
        testService.setMockEntity(entity);

        // When & Then
        assertDoesNotThrow(() -> testService.delete(1L));
    }

    @Test
    void testDelete_NotFound() {
        // Given
        mockDistributedLock();
        testService.setMockEntity(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            testService.delete(1L);
        });
        assertEquals("测试实体不存在: 1", exception.getMessage());
    }

    @Test
    void testDelete_DeleteFailed() {
        // Given
        mockDistributedLock();
        TestEntity entity = new TestEntity();
        entity.setId(1L);
        testService.setMockEntity(entity);
        testService.setDeleteResult(0);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            testService.delete(1L);
        });
        assertEquals("删除测试实体失败", exception.getMessage());
    }

    @Test
    void testGetAll() {
        // Given
        TestEntity entity = new TestEntity();
        entity.setId(1L);
        entity.setName("Test Name");
        testService.setMockEntity(entity);

        // When
        List<TestResponse> responses = testService.getAll();

        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(1L, responses.get(0).getId());
        assertEquals("Test Name", responses.get(0).getName());
    }

    @Test
    void testExecuteWithLock() {
        // Given
        String lockKey = "test:lock";
        String expectedResult = "locked result";

        when(distributedLockUtil.executeWithLock(eq(lockKey), any(Supplier.class)))
                .thenAnswer(invocation -> {
                    Supplier<?> supplier = invocation.getArgument(1);
                    return supplier.get();
                });

        // When
        String result = testService.executeWithLock(lockKey, () -> expectedResult);

        // Then
        assertEquals(expectedResult, result);
        verify(distributedLockUtil).executeWithLock(eq(lockKey), any(Supplier.class));
    }
}
