package com.example.springrediscrac.controller;

import com.example.springrediscrac.model.CacheItem;
import com.example.springrediscrac.service.CacheService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/cache")
@Tag(name = "Cache Operations", description = "Redis cache management operations including CRUD operations, TTL management, and bulk operations")
public class CacheController {

    private static final Logger logger = LoggerFactory.getLogger(CacheController.class);

    @Autowired
    private CacheService cacheService;

    @GetMapping("/{key}")
    @Operation(
        summary = "Retrieve cached value by key",
        description = "Retrieves a cached value from Redis using the specified key. Returns the value if it exists, otherwise returns a 404 Not Found response."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Value found and returned successfully",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = "{\"key\":\"test\",\"value\":\"cached data\",\"exists\":true}"))),
        @ApiResponse(responseCode = "404", description = "Key not found in cache"),
        @ApiResponse(responseCode = "500", description = "Internal server error occurred")
    })
    public ResponseEntity<Map<String, Object>> getValue(
        @Parameter(description = "The cache key to retrieve", example = "user:123")
        @PathVariable String key) {
        logger.info("GET request for key: {}", key);
        
        try {
            Object value = cacheService.getValue(key);
            Map<String, Object> response = new HashMap<>();
            
            if (value != null) {
                response.put("key", key);
                response.put("value", value);
                response.put("exists", true);
                return ResponseEntity.ok(response);
            } else {
                response.put("key", key);
                response.put("value", null);
                response.put("exists", false);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error retrieving value for key: {}", key, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve value", "key", key));
        }
    }

    @PostMapping
    @Operation(
        summary = "Store a new cache item",
        description = "Stores a new key-value pair in the Redis cache. If the key already exists, it will be overwritten."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Cache item created successfully",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = "{\"key\":\"test\",\"value\":\"data\",\"created\":true,\"timestamp\":\"2024-01-01T10:00:00\"}"))),
        @ApiResponse(responseCode = "400", description = "Invalid request body"),
        @ApiResponse(responseCode = "500", description = "Internal server error occurred")
    })
    public ResponseEntity<Map<String, Object>> setValue(
        @Parameter(description = "Cache item containing key and value", required = true)
        @Valid @RequestBody CacheItem cacheItem) {
        logger.info("POST request to set key: {} with value: {}", cacheItem.getKey(), cacheItem.getValue());
        
        try {
            CacheItem stored = cacheService.setValue(cacheItem);
            Map<String, Object> response = new HashMap<>();
            response.put("key", stored.getKey());
            response.put("value", stored.getValue());
            response.put("created", true);
            response.put("timestamp", stored.getCreatedAt());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            logger.error("Error storing value for key: {}", cacheItem.getKey(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to store value", "key", cacheItem.getKey()));
        }
    }

    @PostMapping("/ttl")
    @Operation(
        summary = "Store cache item with TTL",
        description = "Stores a key-value pair in the Redis cache with a specified Time-To-Live (TTL). The item will be automatically removed after the TTL expires."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Cache item with TTL created successfully",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = "{\"key\":\"session:123\",\"value\":\"user data\",\"ttl\":300,\"unit\":\"SECONDS\",\"created\":true}"))),
        @ApiResponse(responseCode = "400", description = "Invalid parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error occurred")
    })
    public ResponseEntity<Map<String, Object>> setValueWithTTL(
            @Parameter(description = "The cache key", example = "session:123")
            @RequestParam String key,
            @Parameter(description = "The value to cache", example = "user session data")
            @RequestParam String value,
            @Parameter(description = "Time to live duration", example = "300")
            @RequestParam(defaultValue = "300") long ttl,
            @Parameter(description = "Time unit for TTL", example = "SECONDS")
            @RequestParam(defaultValue = "SECONDS") String unit) {
        
        logger.info("POST request to set key: {} with TTL: {} {}", key, ttl, unit);
        
        try {
            TimeUnit timeUnit = TimeUnit.valueOf(unit.toUpperCase());
            cacheService.setValueWithTTL(key, value, ttl, timeUnit);
            
            Map<String, Object> response = new HashMap<>();
            response.put("key", key);
            response.put("value", value);
            response.put("ttl", ttl);
            response.put("unit", unit);
            response.put("created", true);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            logger.error("Error storing value with TTL for key: {}", key, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to store value with TTL", "key", key));
        }
    }

    @PutMapping("/{key}")
    @Operation(
        summary = "Update existing cache item",
        description = "Updates the value of an existing cache item. Returns 404 if the key does not exist."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cache item updated successfully",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = "{\"key\":\"test\",\"value\":\"updated data\",\"updated\":true}"))),
        @ApiResponse(responseCode = "400", description = "Invalid request body - value is required"),
        @ApiResponse(responseCode = "404", description = "Key not found in cache"),
        @ApiResponse(responseCode = "500", description = "Internal server error occurred")
    })
    public ResponseEntity<Map<String, Object>> updateValue(
        @Parameter(description = "The cache key to update", example = "user:123")
        @PathVariable String key,
        @Parameter(description = "Request body containing the new value")
        @RequestBody Map<String, Object> payload) {
        logger.info("PUT request to update key: {}", key);
        
        try {
            Object value = payload.get("value");
            if (value == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Value is required", "key", key));
            }
            
            if (!cacheService.hasKey(key)) {
                return ResponseEntity.notFound().build();
            }
            
            Object updatedValue = cacheService.updateValue(key, value);
            Map<String, Object> response = new HashMap<>();
            response.put("key", key);
            response.put("value", updatedValue);
            response.put("updated", true);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error updating value for key: {}", key, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update value", "key", key));
        }
    }

    @DeleteMapping("/{key}")
    @Operation(
        summary = "Delete cache item by key",
        description = "Removes a cache item from Redis using the specified key. Returns 404 if the key does not exist."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cache item deleted successfully",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = "{\"key\":\"test\",\"deleted\":true}"))),
        @ApiResponse(responseCode = "404", description = "Key not found in cache"),
        @ApiResponse(responseCode = "500", description = "Internal server error occurred")
    })
    public ResponseEntity<Map<String, Object>> deleteValue(
        @Parameter(description = "The cache key to delete", example = "user:123")
        @PathVariable String key) {
        logger.info("DELETE request for key: {}", key);
        
        try {
            boolean deleted = cacheService.deleteValue(key);
            Map<String, Object> response = new HashMap<>();
            response.put("key", key);
            response.put("deleted", deleted);
            
            if (deleted) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error deleting value for key: {}", key, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete value", "key", key));
        }
    }

    @GetMapping
    @Operation(
        summary = "Get all cache keys",
        description = "Retrieves all cache keys currently stored in Redis along with the total count."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Keys retrieved successfully",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = "{\"keys\":[\"cache:user:123\",\"cache:session:456\"],\"count\":2}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error occurred")
    })
    public ResponseEntity<Map<String, Object>> getAllKeys() {
        logger.info("GET request for all keys");
        
        try {
            Set<String> keys = cacheService.getAllKeys();
            Map<String, Object> response = new HashMap<>();
            response.put("keys", keys);
            response.put("count", keys.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error retrieving all keys", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve keys"));
        }
    }

    @DeleteMapping
    @Operation(
        summary = "Clear all cache entries",
        description = "Removes all cache entries from Redis. Use with caution as this operation cannot be undone."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "All cache entries cleared successfully",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = "{\"cleared\":true,\"message\":\"All cache entries cleared\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error occurred")
    })
    public ResponseEntity<Map<String, Object>> clearAll() {
        logger.info("DELETE request to clear all cache");
        
        try {
            cacheService.clearAllCache();
            Map<String, Object> response = new HashMap<>();
            response.put("cleared", true);
            response.put("message", "All cache entries cleared");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error clearing all cache", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to clear cache"));
        }
    }

    @GetMapping("/{key}/expiration")
    @Operation(
        summary = "Get cache item expiration",
        description = "Retrieves the remaining time-to-live (TTL) for a cache item in seconds. Returns -1 if the key has no expiration, -2 if the key does not exist."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Expiration information retrieved successfully",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = "{\"key\":\"session:123\",\"expiration\":290,\"unit\":\"seconds\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error occurred")
    })
    public ResponseEntity<Map<String, Object>> getExpiration(
        @Parameter(description = "The cache key to check expiration for", example = "session:123")
        @PathVariable String key) {
        logger.info("GET request for expiration of key: {}", key);
        
        try {
            Long expiration = cacheService.getExpiration(key);
            Map<String, Object> response = new HashMap<>();
            response.put("key", key);
            response.put("expiration", expiration);
            response.put("unit", "seconds");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting expiration for key: {}", key, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get expiration", "key", key));
        }
    }
}