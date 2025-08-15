package com.flyerme.weautotools.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应状态码枚举
 *
 * @author WeAutoTools Team
 * @version 1.0.0
 * @since 2025-08-15
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    // 成功
    SUCCESS(200, "操作成功"),

    // 客户端错误
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),
    CONFLICT(409, "资源冲突"),
    VALIDATION_ERROR(422, "参数校验失败"),

    // 服务器错误
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),
    BAD_GATEWAY(502, "网关错误"),
    SERVICE_UNAVAILABLE(503, "服务不可用"),

    // 业务错误
    BUSINESS_ERROR(1000, "业务处理失败"),
    DATA_NOT_FOUND(1001, "数据不存在"),
    DATA_ALREADY_EXISTS(1002, "数据已存在"),
    OPERATION_NOT_ALLOWED(1003, "操作不被允许"),

    // 数据库错误
    DATABASE_ERROR(2000, "数据库操作失败"),
    DATA_INTEGRITY_VIOLATION(2001, "数据完整性约束违反"),

    // 外部服务错误
    EXTERNAL_SERVICE_ERROR(3000, "外部服务调用失败"),
    NETWORK_ERROR(3001, "网络连接错误"),
    TIMEOUT_ERROR(3002, "请求超时");

    /**
     * 状态码
     */
    private final Integer code;

    /**
     * 状态消息
     */
    private final String message;
}
