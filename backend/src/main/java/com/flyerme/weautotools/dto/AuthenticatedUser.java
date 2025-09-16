package com.flyerme.weautotools.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.List;
import java.util.Map;

/**
 * 认证用户信息传递对象
 * 用于在请求生命周期中传递用户认证信息
 *
 * @author WeAutoTools Team
 * @version 1.0.0
 * @since 2025-09-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticatedUser {

    private Long userId;
    private String username;
    private String userType;
    
    // 用户权限列表
    private List<String> authorities;
    
    /**
     * 用户扩展属性
     */
    @Builder.Default
    private Map<String, Object> attributes = new java.util.HashMap<>();
    

    /**
     * 最后登录时间（UTC时间戳）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Instant lastLoginTime;
    
    /**
     * 客户端IP地址
     */
    private String clientIp;
    
    /**
     * 用户代理信息
     */
    private String userAgent;

    public boolean isAuthenticated() {
        return userId != null && username != null;
    }
    
    /**
     * 判断用户是否具有指定权限
     */
    public boolean hasAuthority(String authority) {
        return authorities != null && authorities.contains(authority);
    }
    
    /**
     * 获取用户标识符（用于限制检查等场景）
     */
    public String getUserIdentifier() {
        return isAuthenticated() ? "user:" + userId : "anonymous:" + clientIp;
    }
    
    /**
     * 设置扩展属性
     */
    public void setAttribute(String key, Object value) {
        if (attributes == null) {
            attributes = new java.util.HashMap<>();
        }
        attributes.put(key, value);
    }
    
    /**
     * 获取扩展属性
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key) {
        return attributes != null ? (T) attributes.get(key) : null;
    }
}