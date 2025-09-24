package com.flyerme.weautotools.service;

import com.flyerme.weautotools.dto.UsageLimitConfigRequest;
import com.flyerme.weautotools.dto.UsageLimitConfigResponse;

import java.util.List;

/**
 * 使用限制配置服务接口
 *
 * @author WeAutoTools Team
 * @version 1.0.2
 * @since 2025-09-12
 */
public interface UsageLimitConfigService {

    /**
     * 获取所有限制配置
     */
    List<UsageLimitConfigResponse> getAllConfigs();

    /**
     * 根据ID获取限制配置
     */
    UsageLimitConfigResponse getConfigById(String id);

    /**
     * 创建限制配置
     */
    UsageLimitConfigResponse createConfig(UsageLimitConfigRequest request);

    /**
     * 更新限制配置
     */
    UsageLimitConfigResponse updateConfig(UsageLimitConfigRequest request);

    /**
     * 删除限制配置
     */
    boolean deleteConfig(String id);

    /**
     * 批量更新配置
     */
    void batchUpdateConfigs(List<UsageLimitConfigRequest> requests);
}