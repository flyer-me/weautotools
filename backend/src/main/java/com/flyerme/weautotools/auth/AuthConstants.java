package com.flyerme.weautotools.auth;

/**
 * 认证相关常量
 *
 * @author WeAutoTools Team
 * @version 1.0.0
 * @since 2025-09-12
 */
public final class AuthConstants {
    
    // RequestAttribute 键名
    public static final String USER_INFO = "AUTHENTICATED_USER";
    public static final String USER_ID = "USER_ID";
    public static final String CLIENT_IP = "CLIENT_IP";
    
    // 用户类型常量
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_USER = "USER";
    public static final String ROLE_ANONYMOUS = "ANONYMOUS";
    
    // Token相关常量
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    
    // 私有构造函数，防止实例化
    private AuthConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}