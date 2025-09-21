package com.flyerme.weautotools.constants;

import com.flyerme.weautotools.entity.Role;

/**
 * 角色常量门面：提供预定义的 Role 实体对象
 * 用法：RoleConstants.ADMIN
 */
public class RoleConstants {
    public static final Role ADMIN = RoleEnum.ADMIN.toRole();
    public static final Role USER = RoleEnum.USER.toRole();
    public static final Role ANONYMOUS = RoleEnum.ANONYMOUS.toRole();
    public static final Role MERCHANT = RoleEnum.MERCHANT.toRole();

    private RoleConstants() {}
}
