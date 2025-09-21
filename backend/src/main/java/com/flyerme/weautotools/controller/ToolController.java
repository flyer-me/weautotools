package com.flyerme.weautotools.controller;

import com.flyerme.weautotools.entity.Tool;
import com.flyerme.weautotools.service.ToolService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.flyerme.weautotools.common.Result;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 工具管理控制器
 *
 * @author WeAutoTools Team
 * @version 1.0.3
 * @since 2025-09-16
 */
@RestController
@RequestMapping("/tools")
@RequiredArgsConstructor
public class ToolController {

    private final ToolService toolService;

    @GetMapping
    public Result<List<Tool>> getActiveTools() {
        List<Tool> tools = toolService.getActiveTools();
        return Result.success(tools);
    }

    @GetMapping("/type/{toolType}")
    public Result<List<Tool>> getToolsByType(@PathVariable String toolType) {
        List<Tool> tools = toolService.getByToolType(toolType);
        return Result.success(tools);
    }


    @GetMapping("/status/{status}")
    public Result<List<Tool>> getToolsByStatus(@PathVariable String status) {
        List<Tool> tools = toolService.getByStatus(status);
        return Result.success(tools);
    }

    @GetMapping("/search")
    public Result<List<Tool>> searchTools(@RequestParam String keyword) {
        List<Tool> tools = toolService.searchByKeyword(keyword);
        return Result.success(tools);
    }

    @PostMapping
    public Result<Tool> createTool(@Valid @RequestBody Tool tool) {
        Tool createdTool = toolService.createTool(tool);
        return Result.success("工具创建成功", createdTool);
    }

    @PutMapping("/{id}")
    public Result<Tool> updateTool(@PathVariable Long id, @Valid @RequestBody Tool tool) {
        tool.setId(id);
        Tool updatedTool = toolService.updateTool(tool);
        return Result.success("工具更新成功", updatedTool);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteTool(@PathVariable Long id) {
        boolean deleted = toolService.deleteTool(id);
        return deleted ? Result.success("工具删除成功") : Result.error("工具不存在");
    }

    @GetMapping("/exists/{toolName}")
    public Result<Boolean> existsByToolCode(@PathVariable String toolName) {
        Boolean exists = toolService.existsByToolName(toolName);
        return Result.success(exists);
    }
}