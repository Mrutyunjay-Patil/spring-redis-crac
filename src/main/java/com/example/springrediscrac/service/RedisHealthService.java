package com.example.springrediscrac.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
public class RedisHealthService {

    private static final Logger logger = LoggerFactory.getLogger(RedisHealthService.class);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    public Map<String, Object> checkHealth() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            // Test basic connection with ping
            RedisConnection connection = redisConnectionFactory.getConnection();
            String ping = connection.ping();
            connection.close();
            
            health.put("status", "UP");
            health.put("ping", ping);
            
            // Test Redis operations
            String testKey = "health:check:" + System.currentTimeMillis();
            String testValue = "test_connection";
            
            redisTemplate.opsForValue().set(testKey, testValue);
            String retrievedValue = (String) redisTemplate.opsForValue().get(testKey);
            redisTemplate.delete(testKey);
            
            health.put("redis_test", "test_connection".equals(retrievedValue) ? "PASS" : "FAIL");
            
            // Get basic server info
            try {
                Set<String> keys = redisTemplate.keys("*");
                long keyCount = keys != null ? keys.size() : 0;
                health.put("key_count", keyCount);
                health.put("client_name", "Lettuce");
            } catch (Exception e) {
                logger.warn("Could not retrieve Redis server info", e);
                health.put("server_info", "N/A");
            }
            
        } catch (Exception e) {
            logger.error("Redis health check failed", e);
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
        }
        
        return health;
    }

    public boolean isHealthy() {
        try {
            // Test if we can access Redis using RedisTemplate
            RedisConnection connection = redisConnectionFactory.getConnection();
            String ping = connection.ping();
            connection.close();
            return "PONG".equals(ping);
        } catch (Exception e) {
            logger.error("Redis health check failed", e);
            return false;
        }
    }
}