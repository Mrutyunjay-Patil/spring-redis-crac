package com.example.springrediscrac.crac;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class RedisCracResource {

    private static final Logger logger = LoggerFactory.getLogger(RedisCracResource.class);

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @PostConstruct
    public void init() {
        logger.info("RedisCracResource initialized - Using Lettuce Redis client with Spring Boot CRaC support");
        logger.info("Cache manager: {}", cacheManager.getClass().getSimpleName());
        logger.info("Connection factory: {}", redisConnectionFactory.getClass().getSimpleName());
    }
}