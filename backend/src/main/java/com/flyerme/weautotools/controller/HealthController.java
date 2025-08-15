package com.flyerme.weautotools.controller;

import com.flyerme.weautotools.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 *
 * @author WeAutoTools Team
 * @version 1.0.0
 * @since 2025-08-15
 */
@RestController
@RequestMapping("/health")
public class HealthController {

    /**
     * 健康检查接口
     */
    @GetMapping
    public Result<Map<String, Object>> health() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "UP");
        data.put("timestamp", LocalDateTime.now());
        data.put("application", "WeAutoTools Backend");
        data.put("version", "1.0.0");
        
        return Result.success("服务运行正常", data);
    }

    /**
     * 简单的Hello World接口
     */
    @GetMapping("/hello")
    public Result<String> hello() {
        return Result.success("Hello, WeAutoTools!");
    }
}
