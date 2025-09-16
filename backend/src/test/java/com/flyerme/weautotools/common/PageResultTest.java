package com.flyerme.weautotools.common;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PageResult 测试类
 *
 * @author WeAutoTools Team
 * @version 1.0.0
 * @since 2025-01-20
 */
class PageResultTest {

    @Test
    void testPageResultCreation() {
        List<String> records = List.of("item1", "item2", "item3");
        PageResult<String> pageResult = PageResult.of(records, 10L, 5L, 1L);
        
        assertEquals(records, pageResult.getRecords());
        assertEquals(10L, pageResult.getTotal());
        assertEquals(5L, pageResult.getSize());
        assertEquals(1L, pageResult.getCurrent());
        assertEquals(2L, pageResult.getPages()); // (10 + 5 - 1) / 5 = 2
    }

    @Test
    void testPageResultEmpty() {
        PageResult<String> pageResult = PageResult.empty();
        
        assertTrue(pageResult.isEmpty());
        assertEquals(0L, pageResult.getTotal());
        assertEquals(10L, pageResult.getSize());
        assertEquals(1L, pageResult.getCurrent());
        assertEquals(0L, pageResult.getPages());
    }

    @Test
    void testPageResultEmptyWithParams() {
        PageResult<String> pageResult = PageResult.empty(20L, 2L);
        
        assertTrue(pageResult.isEmpty());
        assertEquals(0L, pageResult.getTotal());
        assertEquals(20L, pageResult.getSize());
        assertEquals(2L, pageResult.getCurrent());
        assertEquals(0L, pageResult.getPages());
    }

    @Test
    void testHasNext() {
        PageResult<String> pageResult = PageResult.of(List.of("item1"), 10L, 5L, 1L);
        assertTrue(pageResult.hasNext());
        
        PageResult<String> lastPageResult = PageResult.of(List.of("item1"), 10L, 5L, 2L);
        assertFalse(lastPageResult.hasNext());
    }

    @Test
    void testHasPrevious() {
        PageResult<String> firstPageResult = PageResult.of(List.of("item1"), 10L, 5L, 1L);
        assertFalse(firstPageResult.hasPrevious());
        
        PageResult<String> secondPageResult = PageResult.of(List.of("item1"), 10L, 5L, 2L);
        assertTrue(secondPageResult.hasPrevious());
    }

    @Test
    void testIsEmpty() {
        PageResult<String> emptyResult = PageResult.of(List.of(), 0L, 5L, 1L);
        assertTrue(emptyResult.isEmpty());
        
        PageResult<String> nonEmptyResult = PageResult.of(List.of("item1"), 1L, 5L, 1L);
        assertFalse(nonEmptyResult.isEmpty());
    }
}