package com.flyerme.weautotools.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis 配置类
 *
 * @author WeAutoTools Team
 * @version 1.0.0
 * @since 2025-08-15
 */
@Configuration
@MapperScan("com.flyerme.weautotools.mapper")
public class MyBatisConfig {
    // 使用Spring Boot MyBatis自动配置
    // 配置项通过application.yml进行设置
}
