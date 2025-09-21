package com.flyerme.weautotools.controller;

import com.flyerme.weautotools.entity.Role;
import com.flyerme.weautotools.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    public List<Role> list() {
        return roleService.list();
    }

    /**
     * 根据 code 查询角色
     */
    @GetMapping("/{name}")
    public Role getByCode(@PathVariable String name) {
        return roleService.getByName(name)
                .orElseThrow(() -> new IllegalArgumentException("角色不存在: " + name));
    }

    /**
     * 初始化默认角色（管理接口）
     */
    @PostMapping("/init")
    public String init() {
        roleService.initDefaultRoles();
        return "默认角色初始化完成";
    }
}