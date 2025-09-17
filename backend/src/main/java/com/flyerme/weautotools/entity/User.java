package com.flyerme.weautotools.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.flyerme.weautotools.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("users")
public class User extends BaseEntity {

    private String mobile;

    private String passwordHash;

    private String role;

    private Boolean enabled = true;

    private Boolean accountNonLocked = true;

    private Boolean credentialsNonExpired = true;

    private Boolean accountNonExpired = true;

    private String nickname;

    private String avatarUrl;

    @TableField(exist = false)
    private List<String> roles;

    public String getUsername() {
        return mobile;
    }
}
