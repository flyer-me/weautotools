package com.flyerme.weautotools.controller;

import com.flyerme.weautotools.entity.Tool;
import com.flyerme.weautotools.service.ToolService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<List<Tool>> getActiveTools() {
        return ResponseEntity.ok(toolService.getActiveTools());
    }

    @GetMapping("/type/{toolType}")
    public ResponseEntity<List<Tool>> getToolsByType(@PathVariable String toolType) {
        return ResponseEntity.ok(toolService.getByToolType(toolType));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Tool>> getToolsByCategory(@PathVariable String category) {
        return ResponseEntity.ok(toolService.getByCategory(category));
    }

    @GetMapping("/frontend")
    public ResponseEntity<List<Tool>> getFrontendTools() {
        return ResponseEntity.ok(toolService.getFrontendTools());
    }

    @GetMapping("/backend")
    public ResponseEntity<List<Tool>> getBackendTools() {
        return ResponseEntity.ok(toolService.getBackendTools());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Tool>> getToolsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(toolService.getByStatus(status));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Tool>> searchTools(@RequestParam String keyword) {
        return ResponseEntity.ok(toolService.searchByKeyword(keyword));
    }

    @GetMapping("/{toolCode}")
    public ResponseEntity<Tool> getToolByCode(@PathVariable String toolCode) {
        return toolService.getByToolCode(toolCode)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Tool> createTool(@Valid @RequestBody Tool tool) {
        return ResponseEntity.ok(toolService.createTool(tool));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Tool> updateTool(@PathVariable Long id, @Valid @RequestBody Tool tool) {
        tool.setId(id);
        return ResponseEntity.ok(toolService.updateTool(tool));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTool(@PathVariable Long id) {
        boolean deleted = toolService.deleteTool(id);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/exists/{toolCode}")
    public ResponseEntity<Boolean> existsByToolCode(@PathVariable String toolCode) {
        return ResponseEntity.ok(toolService.existsByToolCode(toolCode));
    }

    @GetMapping("/stats/status")
    public ResponseEntity<Map<String, Integer>> countByStatus() {
        return ResponseEntity.ok(toolService.countByStatus());
    }

    @GetMapping("/stats/category")
    public ResponseEntity<Map<String, Integer>> countByCategory() {
        return ResponseEntity.ok(toolService.countByCategory());
    }

    @GetMapping("/{toolCode}/id")
    public ResponseEntity<Long> getToolIdByCode(@PathVariable String toolCode) {
        Long toolId = toolService.getToolIdByCode(toolCode);
        return toolId != null ? ResponseEntity.ok(toolId) : ResponseEntity.notFound().build();
    }
}