package com.flyerme.weautotools.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.flyerme.weautotools.constants.RoleConstants;
import com.flyerme.weautotools.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

@Mapper
public interface RoleMapper extends BaseMapper<Role> {
    @Select("SELECT * FROM roles WHERE name = #{roleName}")
    Optional<Role> selectByRoleName(String roleName);

    /**
     * 获取默认角色，该返回值应为 ROLE_USER
     * @return ROLE_USER对应实体
     */
    default Role findDefaultRole(){
        return selectByRoleName(RoleConstants.USER.getName())
                .orElseThrow(() -> new IllegalStateException(
                        "默认Role不存在！请检查数据库初始化。Role: " + RoleConstants.USER.getName()
                ));
    }

    @Select("""
    SELECT r.id, r.name
    FROM roles r
    INNER JOIN user_roles ur ON r.id = ur.role_id
    WHERE ur.user_id = #{userId}
""")
    List<Role> getRolesByUserId(Long id);
}
