package com.flyerme.weautotools.dto;

import jakarta.validation.constraints.*;

/**
 * 用户注册请求DTO
 *
 * @author WeAutoTools Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public record RegisterRequest(
        @NotNull(message = "注册方式必须是合法值")
        RegisterType registerType,

        @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "手机号格式不正确")
        String mobile,

        @Email(message = "邮箱格式不正确")
        String email,

        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 20, message = "密码长度必须在6-20位之间")
        String password,

        @Size(max = 50, message = "昵称长度不能超过50字符")
        String nickname
)
{
    /**
     * 获取用户标识
     * @return 用户标识，手机号或邮箱等
     */
    public String getIdentifier() {
        switch (registerType)
        {
            case PHONE -> { return mobile;}
            case EMAIL -> { return email;}
            default -> { return "";}
        }
    }
}

