package com.flyerme.weautotools.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.flyerme.weautotools.entity.UserRole;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

import java.util.UUID;

@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {

    @Insert("insert into user_roles (user_id, role_id) values (#{userId}, #{roleId})")
    int save(UUID userId, UUID roleId);
}
