package com.flyerme.weautotools.auth;

import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;

@Service
public class JwtTokenBlacklist {

    private final RedissonClient redissonClient;

    public JwtTokenBlacklist(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    public void add(String token, Date expiration) {
        if (token != null && expiration != null) {
            long ttl = expiration.getTime() - System.currentTimeMillis();
            if (ttl > 0) {
                RBucket<String> bucket = redissonClient.getBucket("blacklist:" + token);
                bucket.set("blacklisted", Duration.ofMillis(ttl));
            }
        }
    }

    public boolean isBlacklisted(String token) {
        RBucket<String> bucket = redissonClient.getBucket("blacklist:" + token);
        return bucket.isExists();
    }
}
