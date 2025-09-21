package com.flyerme.weautotools.service.impl;

import com.flyerme.weautotools.dao.RoleMapper;
import com.flyerme.weautotools.dao.UserMapper;
import com.flyerme.weautotools.dao.UserRoleMapper;
import com.flyerme.weautotools.dto.RegisterRequest;
import com.flyerme.weautotools.dto.RegisterType;
import com.flyerme.weautotools.entity.Role;
import com.flyerme.weautotools.entity.User;
import com.flyerme.weautotools.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;

    
    @Override
    public void assignDefaultRole(User user) {
        Role role = roleMapper.findDefaultRole();
        userRoleMapper.save(user.getId(), role.getId());
    }
    
    @Override
    @Transactional
    public User createUserWithDefaultRole(RegisterRequest request) {
        User user = new User();
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setEnabled(true);
        user.setLockedUntil(null);

        if (request.registerType() == RegisterType.PHONE) {
            user.setMobile(request.mobile());
        } else {
            user.setEmail(request.email());
        }

        if (request.nickname() != null) {
            user.setNickname(request.nickname());
        }
        userMapper.insert(user);
        assignDefaultRole(user);
        return user;
    }
}