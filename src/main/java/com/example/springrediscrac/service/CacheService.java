package com.example.springrediscrac.service;

import com.example.springrediscrac.model.CacheItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class CacheService {

    private static final Logger logger = LoggerFactory.getLogger(CacheService.class);
    private static final String CACHE_KEY_PREFIX = "cache:";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Cacheable(value = "cache", key = "#key")
    public Object getValue(String key) {
        logger.info("Retrieving value for key: {}", key);
        String redisKey = CACHE_KEY_PREFIX + key;
        Object value = redisTemplate.opsForValue().get(redisKey);
        logger.debug("Retrieved value: {} for key: {}", value, key);
        return value;
    }

    @CachePut(value = "cache", key = "#cacheItem.key")
    public CacheItem setValue(CacheItem cacheItem) {
        logger.info("Storing value for key: {}", cacheItem.getKey());
        String redisKey = CACHE_KEY_PREFIX + cacheItem.getKey();
        redisTemplate.opsForValue().set(redisKey, cacheItem.getValue());
        logger.debug("Stored value: {} for key: {}", cacheItem.getValue(), cacheItem.getKey());
        return cacheItem;
    }

    @CachePut(value = "cache", key = "#key")
    public Object updateValue(String key, Object value) {
        logger.info("Updating value for key: {}", key);
        String redisKey = CACHE_KEY_PREFIX + key;
        redisTemplate.opsForValue().set(redisKey, value);
        logger.debug("Updated value: {} for key: {}", value, key);
        return value;
    }

    @CacheEvict(value = "cache", key = "#key")
    public boolean deleteValue(String key) {
        logger.info("Deleting value for key: {}", key);
        String redisKey = CACHE_KEY_PREFIX + key;
        Boolean result = redisTemplate.delete(redisKey);
        logger.debug("Deleted key: {}, result: {}", key, result);
        return Boolean.TRUE.equals(result);
    }

    public boolean hasKey(String key) {
        String redisKey = CACHE_KEY_PREFIX + key;
        return Boolean.TRUE.equals(redisTemplate.hasKey(redisKey));
    }

    public Set<String> getAllKeys() {
        Set<String> keys = redisTemplate.keys(CACHE_KEY_PREFIX + "*");
        return keys != null ? keys : new HashSet<>();
    }

    public void setValueWithTTL(String key, Object value, long timeout, TimeUnit unit) {
        logger.info("Storing value with TTL for key: {}, timeout: {} {}", key, timeout, unit);
        String redisKey = CACHE_KEY_PREFIX + key;
        redisTemplate.opsForValue().set(redisKey, value, timeout, unit);
    }

    public Long getExpiration(String key) {
        String redisKey = CACHE_KEY_PREFIX + key;
        return redisTemplate.getExpire(redisKey, TimeUnit.MILLISECONDS);
    }

    @CacheEvict(value = "cache", allEntries = true)
    public void clearAllCache() {
        logger.info("Clearing all cache entries");
        Set<String> keys = redisTemplate.keys(CACHE_KEY_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            logger.info("Cleared {} cache entries", keys.size());
        }
    }
}