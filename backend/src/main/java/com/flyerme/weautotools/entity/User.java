package com.flyerme.weautotools.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.flyerme.weautotools.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("users")
public class User extends BaseEntity {

    private String passwordHash;

    private Boolean enabled = true;

    private String mobile;

    private String email;

    private String nickname;

    private String avatarUrl;

    private Instant credentialsExpiry;

    private Instant lockedUntil;

    private Integer failedAttempts;

    public String getUsername() {
        return mobile;
    }
}