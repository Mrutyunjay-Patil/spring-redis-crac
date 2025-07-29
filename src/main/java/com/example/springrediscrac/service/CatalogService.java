package com.example.springrediscrac.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class CatalogService {

    private static final Logger logger = LoggerFactory.getLogger(CatalogService.class);

    @Cacheable(value = "cache_collection_attribute", key = "#id")
    public String getCachedData(String id) {
        logger.info("Fetching data from expensive operation for id: {}", id);
        // Simulate expensive operation
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return "Expensive data for " + id + " at " + System.currentTimeMillis();
    }

    @CachePut(value = "cache_collection_attribute", key = "#id")
    public String updateCache(String id, String data) {
        logger.info("Updating cache for id: {} with data: {}", id, data);
        return data;
    }

    @CacheEvict(value = "cache_collection_attribute", key = "#id")
    public void evictCache(String id) {
        logger.info("Evicting cache for id: {}", id);
    }

    @CacheEvict(value = "cache_collection_attribute", allEntries = true)
    public void evictAllCache() {
        logger.info("Evicting all cache entries");
    }
}