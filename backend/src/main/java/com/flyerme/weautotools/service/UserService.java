package com.flyerme.weautotools.service;

import com.flyerme.weautotools.dto.RegisterRequest;
import com.flyerme.weautotools.entity.User;

public interface UserService {
    
    /**
     * 为用户分配默认角色
     * @param user 用户实体
     */
    void assignDefaultRole(User user);
    
    /**
     * 创建用户并分配默认角色
     * 在一个事务中完成用户创建和角色分配
     * @param request 注册请求
     * @return 创建的用户实体
     */
    User createUserWithDefaultRole(RegisterRequest request);
}