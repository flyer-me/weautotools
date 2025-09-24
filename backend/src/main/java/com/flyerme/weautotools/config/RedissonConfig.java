package com.flyerme.weautotools.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson配置类
 * 配置Redis分布式锁客户端
 *
 * @author WeAutoTools Team
 * @version 1.0.0
 * @since 2025-08-17
 */
@Configuration
public class RedissonConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    @Value("${spring.data.redis.database:0}")
    private int redisDatabase;

    @Value("${spring.data.redis.timeout:3000ms}")
    private String redisTimeout;

    // 注入自定义配置的ObjectMapper
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 配置Redisson客户端
     */
    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        
        // 单机模式配置
        String redisUrl = String.format("redis://%s:%d", redisHost, redisPort);
        config.useSingleServer()
                .setAddress(redisUrl)
                .setDatabase(redisDatabase)
                .setConnectionMinimumIdleSize(5)
                .setConnectionPoolSize(20)
                .setIdleConnectionTimeout(10000)
                .setConnectTimeout(3000)
                .setTimeout(3000)
                .setRetryAttempts(3)
                .setRetryInterval(1500);

        // 如果有密码则设置密码
        if (redisPassword != null && !redisPassword.trim().isEmpty()) {
            config.useSingleServer().setPassword(redisPassword);
        }

        // 使用配置了自定义反序列化器的ObjectMapper创建编解码器
        config.setCodec(new JsonJacksonCodec(objectMapper));

        return Redisson.create(config);
    }
}