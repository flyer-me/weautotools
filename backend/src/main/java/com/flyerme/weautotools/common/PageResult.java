package com.flyerme.weautotools.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 分页响应结果封装类
 *
 * @author WeAutoTools Team
 * @version 1.0.0
 * @since 2025-01-20
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageResult<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 数据列表
     */
    private List<T> records;

    private Long total;

    /**
     * 每页大小
     */
    private Long size;

    /**
     * 当前页码（从1开始）
     */
    private Long current;

    /**
     * 总页数
     */
    private Long pages;

    public PageResult() {
    }

    public PageResult(List<T> records, Long total, Long size, Long current) {
        this.records = records;
        this.total = total;
        this.size = size;
        this.current = current;
        this.pages = (total + size - 1) / size; // 计算总页数，向上取整
    }

    /**
     * 创建空分页结果
     */
    public static <T> PageResult<T> empty() {
        return new PageResult<>(List.of(), 0L, 10L, 1L);
    }

    /**
     * 创建空分页结果（指定分页参数）
     */
    public static <T> PageResult<T> empty(Long size, Long current) {
        return new PageResult<>(List.of(), 0L, size, current);
    }

    /**
     * 创建分页结果
     */
    public static <T> PageResult<T> of(List<T> records, Long total, Long size, Long current) {
        return new PageResult<>(records, total, size, current);
    }

    /**
     * 判断是否有下一页
     */
    public boolean hasNext() {
        return current != null && pages != null && current < pages;
    }

    /**
     * 判断是否有上一页
     */
    public boolean hasPrevious() {
        return current != null && current > 1;
    }

    /**
     * 判断是否为空
     */
    public boolean isEmpty() {
        return records == null || records.isEmpty();
    }
}