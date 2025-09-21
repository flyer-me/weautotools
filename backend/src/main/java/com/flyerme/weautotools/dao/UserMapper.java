package com.flyerme.weautotools.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.flyerme.weautotools.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.Optional;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    /**
     * 根据手机号查询用户
     */
    @Select("SELECT * FROM users WHERE mobile = #{mobile}")
    Optional<User> selectByMobile(String mobile);
    
    /**
     * 检查手机号是否已存在
     */
    @Select("SELECT COUNT(*) FROM users WHERE mobile = #{mobile}")
    int countByMobile(String mobile);

    @Select("SELECT * FROM users WHERE email = #{username}")
    Optional<User> findByEmail(String username);

    @Select("SELECT * FROM users WHERE mobile = #{mobile}")
    Optional<User> findByPhone(String mobile);

    @Select("SELECT COUNT(*) FROM users WHERE email = #{email}")
    int countByEmail(String email);
}
