package com.flyerme.weautotools.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

@Data
@TableName("roles")
public class Role {

    private UUID id;

    private String name; // 如 "ROLE_ADMIN"

    private String description;

    public Role(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public Role() {}
}
