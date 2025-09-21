package com.flyerme.weautotools.constants;

import com.flyerme.weautotools.entity.Role;
import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
public enum RoleEnum {
    USER("ROLE_USER", "普通用户"),
    ADMIN("ROLE_ADMIN", "管理员"),
    MERCHANT("ROLE_EDITOR", "内容编辑"),
    ANONYMOUS("ROLE_ANONYMOUS", "匿名用户");

    private final String name;
    private final String description;

    RoleEnum(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public static Optional<RoleEnum> findByName(String name) {
        return Arrays.stream(values())
                .filter(r -> r.getName().equals(name))
                .findFirst();
    }

    /**
     * 转换为 Role 实体（用于数据库操作）
     */
    public Role toRole() {
        Role role = new Role();
        role.setName(this.name);
        role.setDescription(this.description);
        return role;
    }
}
