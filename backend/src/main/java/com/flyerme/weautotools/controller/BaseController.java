package com.flyerme.weautotools.controller;

import com.flyerme.weautotools.auth.AuthenticationCenterService;
import com.flyerme.weautotools.dto.AuthenticatedUser;
import com.flyerme.weautotools.entity.ToolUsageLimit;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 基础控制器类 - 涉及用户信息获取请继承此类
 *
 * @author WeAutoTools Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@Slf4j
public abstract class BaseController {

    @Autowired
    protected AuthenticationCenterService authenticationCenterService;

    /**
     * 获取当前认证用户信息
     */
    protected AuthenticatedUser getCurrentUser(HttpServletRequest request) {
        try {
            return authenticationCenterService.getCurrentUser(request);
        } catch (Exception e) {
            log.error("Error getting current user", e);
            return null;
        }
    }

    /**
     * 检查用户是否已认证
     */
    protected boolean isAuthenticated(HttpServletRequest request) {
        try {
            AuthenticatedUser user = authenticationCenterService.getCurrentUser(request);
            return user != null && user.isAuthenticated();
        } catch (Exception e) {
            log.error("Error checking authentication status", e);
            return false;
        }
    }

    /**
     * 获取用户标识符（用于使用限制等功能）
     */
    protected String getUserIdentifier(HttpServletRequest request) {
        try {
            AuthenticatedUser user = authenticationCenterService.getCurrentUser(request);
            return user != null ? user.getUserIdentifier() : "anonymous";
        } catch (Exception e) {
            log.error("Error getting user identifier", e);
            return "anonymous";
        }
    }

    /**
     * 获取用户类型（用于使用限制等功能）
     */
    protected ToolUsageLimit.UserType getUserType(HttpServletRequest request) {
        return isAuthenticated(request) ? 
               ToolUsageLimit.UserType.LOGIN : 
               ToolUsageLimit.UserType.ANONYMOUS;
    }
}