package com.flyerme.weautotools.common;

import com.flyerme.weautotools.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Initializer implements CommandLineRunner {

    private final RoleService roleService;

    @Override
    public void run(String... args) {
        roleService.initDefaultRoles();
        System.out.println("检查了默认Role(s)初始化");
    }
}
