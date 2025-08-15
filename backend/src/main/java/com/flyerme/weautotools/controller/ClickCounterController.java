package com.flyerme.weautotools.controller;

import com.flyerme.weautotools.common.Result;
import com.flyerme.weautotools.dto.ClickCounterRequest;
import com.flyerme.weautotools.dto.ClickCounterResponse;
import com.flyerme.weautotools.service.ClickCounterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 点击计数器控制器
 *
 * @author WeAutoTools Team
 * @version 1.0.0
 * @since 2025-08-15
 */
@Slf4j
@RestController
@RequestMapping("/click-counter")
@RequiredArgsConstructor
public class ClickCounterController {

    private final ClickCounterService clickCounterService;

    /**
     * 创建计数器
     */
    @PostMapping
    public Result<ClickCounterResponse> createCounter(@Valid @RequestBody ClickCounterRequest request) {
        log.info("创建计数器请求: {}", request.getCounterName());
        ClickCounterResponse response = clickCounterService.createCounter(request);
        return Result.success("计数器创建成功", response);
    }

    /**
     * 根据ID获取计数器
     */
    @GetMapping("/{id}")
    public Result<ClickCounterResponse> getCounter(@PathVariable Long id) {
        ClickCounterResponse response = clickCounterService.getCounterById(id);
        return Result.success(response);
    }

    /**
     * 根据名称获取计数器
     */
    @GetMapping("/name/{counterName}")
    public Result<ClickCounterResponse> getCounterByName(@PathVariable String counterName) {
        ClickCounterResponse response = clickCounterService.getCounterByName(counterName);
        return Result.success(response);
    }

    /**
     * 获取所有计数器
     */
    @GetMapping
    public Result<List<ClickCounterResponse>> getAllCounters() {
        List<ClickCounterResponse> responses = clickCounterService.getAllCounters();
        return Result.success(responses);
    }

    /**
     * 获取所有启用的计数器
     */
    @GetMapping("/enabled")
    public Result<List<ClickCounterResponse>> getEnabledCounters() {
        List<ClickCounterResponse> responses = clickCounterService.getEnabledCounters();
        return Result.success(responses);
    }

    /**
     * 更新计数器
     */
    @PutMapping("/{id}")
    public Result<ClickCounterResponse> updateCounter(@PathVariable Long id, 
                                                     @Valid @RequestBody ClickCounterRequest request) {
        log.info("更新计数器请求: {} -> {}", id, request.getCounterName());
        ClickCounterResponse response = clickCounterService.updateCounter(id, request);
        return Result.success("计数器更新成功", response);
    }

    /**
     * 删除计数器
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteCounter(@PathVariable Long id) {
        log.info("删除计数器请求: {}", id);
        clickCounterService.deleteCounter(id);
        return Result.success("计数器删除成功");
    }

    /**
     * 点击计数（根据ID）
     */
    @PostMapping("/{id}/click")
    public Result<ClickCounterResponse> clickCounter(@PathVariable Long id) {
        ClickCounterResponse response = clickCounterService.clickCounter(id);
        return Result.success("点击成功", response);
    }

    /**
     * 点击计数（根据名称）
     */
    @PostMapping("/name/{counterName}/click")
    public Result<ClickCounterResponse> clickCounterByName(@PathVariable String counterName) {
        ClickCounterResponse response = clickCounterService.clickCounterByName(counterName);
        return Result.success("点击成功", response);
    }

    /**
     * 获取统计信息
     */
    @GetMapping("/statistics")
    public Result<ClickCounterService.ClickCounterStatistics> getStatistics() {
        ClickCounterService.ClickCounterStatistics statistics = clickCounterService.getStatistics();
        return Result.success(statistics);
    }
}
