package com.flyerme.weautotools.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.flyerme.weautotools.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    //@Select("SELECT user_id, mobile as username, password_hash as password, roles FROM users WHERE mobile = #{mobile}")
    Optional<User> findByUsername(String mobile);
}
