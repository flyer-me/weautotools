package com.flyerme.weautotools.controller;

import com.flyerme.weautotools.common.Result;
import com.flyerme.weautotools.dto.UsageLimitCheckResponse;
import com.flyerme.weautotools.dto.UsageLimitConfigRequest;
import com.flyerme.weautotools.dto.UsageLimitConfigResponse;
import com.flyerme.weautotools.entity.ToolUsageLimit;
import com.flyerme.weautotools.service.UsageLimitService;
import com.flyerme.weautotools.service.UsageLimitConfigService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * UsageLimit
 * 使用新的BaseController和内部网关获取用户信息
 *
 * @author WeAutoTools Team
 * @version 1.0.2
 * @since 2025-09-12
 */
@RestController
@RequestMapping("/api/usage-limits")
@RequiredArgsConstructor
@Slf4j
public class UsageLimitController extends BaseController {

    private final UsageLimitService usageLimitService;
    private final UsageLimitConfigService usageLimitConfigService;

    /**
     * 检查工具使用限制
     * 使用新的用户信息获取方式
     */
    @GetMapping("/check")
    public Result<UsageLimitCheckResponse> checkUsageLimit(
            @RequestParam String toolName,
            HttpServletRequest request) {
        try {
            // 使用BaseController的方法获取用户信息
            String userIdentifier = getUserIdentifier(request);
            ToolUsageLimit.UserType userType = getUserType(request);
            
            log.debug("Checking usage limit for tool: {}, user: {}, type: {}", 
                     toolName, userIdentifier, userType);
            
            // 检查限制
            boolean isExceeded = usageLimitService.isExceededLimit(userIdentifier, toolName, userType);
            
            if (isExceeded) {
                return Result.success(UsageLimitCheckResponse.exceeded(
                    toolName, userType.name(), "使用次数已达限制，请稍后再试或登录获取更多使用次数"
                ));
            } else {
                int remaining = usageLimitService.getRemainingUsage(userIdentifier, toolName, userType);
                return Result.success(UsageLimitCheckResponse.success(toolName, userType.name(), remaining));
            }
        } catch (Exception e) {
            log.error("检查使用限制异常", e);
            return Result.error("检查使用限制失败");
        }
    }

    /**
     * 获取所有限制配置
     */
    @GetMapping("/configs")
    public Result<List<UsageLimitConfigResponse>> getAllConfigs() {
        try {
            List<UsageLimitConfigResponse> configs = usageLimitConfigService.getAllConfigs();
            return Result.success(configs);
        } catch (Exception e) {
            log.error("获取限制配置异常", e);
            return Result.error("获取限制配置失败");
        }
    }

    /**
     * 根据ID获取限制配置
     */
    @GetMapping("/configs/{id}")
    public Result<UsageLimitConfigResponse> getConfigById(@PathVariable Long id) {
        try {
            UsageLimitConfigResponse config = usageLimitConfigService.getConfigById(id);
            if (config == null) {
                return Result.error("配置不存在");
            }
            return Result.success(config);
        } catch (Exception e) {
            log.error("获取限制配置异常", e);
            return Result.error("获取限制配置失败");
        }
    }

    /**
     * 创建限制配置
     */
    @PostMapping("/configs")
    public Result<UsageLimitConfigResponse> createConfig(@Valid @RequestBody UsageLimitConfigRequest request) {
        try {
            UsageLimitConfigResponse config = usageLimitConfigService.createConfig(request);
            return Result.success(config);
        } catch (Exception e) {
            log.error("创建限制配置异常", e);
            return Result.error("创建限制配置失败: " + e.getMessage());
        }
    }

    /**
     * 更新限制配置
     */
    @PutMapping("/configs/{id}")
    public Result<UsageLimitConfigResponse> updateConfig(
            @PathVariable Long id, 
            @Valid @RequestBody UsageLimitConfigRequest request) {
        try {
            request.setId(id);
            UsageLimitConfigResponse config = usageLimitConfigService.updateConfig(request);
            if (config == null) {
                return Result.error("配置不存在");
            }
            return Result.success(config);
        } catch (Exception e) {
            log.error("更新限制配置异常", e);
            return Result.error("更新限制配置失败: " + e.getMessage());
        }
    }

    /**
     * 删除限制配置
     */
    @DeleteMapping("/configs/{id}")
    public Result<Void> deleteConfig(@PathVariable Long id) {
        try {
            boolean success = usageLimitConfigService.deleteConfig(id);
            if (!success) {
                return Result.error("配置不存在");
            }
            return Result.success();
        } catch (Exception e) {
            log.error("删除限制配置异常", e);
            return Result.error("删除限制配置失败");
        }
    }

    /**
     * 批量更新配置
     */
    @PutMapping("/configs/batch")
    public Result<Void> batchUpdateConfigs(@Valid @RequestBody List<UsageLimitConfigRequest> requests) {
        try {
            usageLimitConfigService.batchUpdateConfigs(requests);
            return Result.success();
        } catch (Exception e) {
            log.error("批量更新配置异常", e);
            return Result.error("批量更新配置失败: " + e.getMessage());
        }
    }

    /**
     * 重置用户使用次数
     */
    @PostMapping("/reset")
    public Result<Void> resetUserUsage(@RequestParam String userIdentifier) {
        try {
            usageLimitService.resetUserUsage(userIdentifier);
            return Result.success();
        } catch (Exception e) {
            log.error("重置用户使用次数异常", e);
            return Result.error("重置用户使用次数失败");
        }
    }


}