package com.flyerme.weautotools.service;

import com.flyerme.weautotools.constants.RoleEnum;
import com.flyerme.weautotools.dao.RoleMapper;
import com.flyerme.weautotools.entity.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleMapper roleMapper;

    public List<Role> list() {
        return roleMapper.selectList(null);
    }

    public Optional<Role> getByName(String code) {
        return roleMapper.selectByRoleName(code);
    }

    public void initDefaultRoles() {
        var roles = Arrays.stream(RoleEnum.values())
                .filter(role -> role != RoleEnum.ANONYMOUS)
                .toList();
        for (RoleEnum roleEnum : roles) {
            var existing = roleMapper.selectByRoleName(roleEnum.getName());
            if (existing.isEmpty()) {
                Role role = roleEnum.toRole();
                roleMapper.insert(role);
            }
        }
    }

}