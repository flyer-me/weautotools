package com.flyerme.weautotools.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("roles")
public class Role {

    private Long id;

    private String name; // 如 "ROLE_ADMIN"

    private String description;

    public Role(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public Role() {}
}
