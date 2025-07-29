package com.example.springrediscrac.controller;

import com.example.springrediscrac.service.RedisHealthService;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health")
@Tag(name = "Health Monitoring", description = "Application and Redis health check endpoints for monitoring service availability")
public class HealthController {

    private static final Logger logger = LoggerFactory.getLogger(HealthController.class);

    @Autowired
    private RedisHealthService redisHealthService;

    @GetMapping("/redis")
    @Operation(
        summary = "Detailed Redis health check",
        description = "Performs a comprehensive health check of the Redis connection including ping test, template operations, and server information."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Redis is healthy and operational",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = "{\"status\":\"UP\",\"ping\":\"PONG\",\"template_test\":\"PASS\",\"server_info\":{...}}"))),
        @ApiResponse(responseCode = "503", description = "Redis is unhealthy or not available",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = "{\"status\":\"DOWN\",\"error\":\"Connection failed\"}")))
    })
    public ResponseEntity<Map<String, Object>> checkRedisHealth() {
        logger.info("Redis health check requested");
        
        try {
            Map<String, Object> healthInfo = redisHealthService.checkHealth();
            String status = (String) healthInfo.get("status");
            
            if ("UP".equals(status)) {
                return ResponseEntity.ok(healthInfo);
            } else {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(healthInfo);
            }
        } catch (Exception e) {
            logger.error("Error performing Redis health check", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "DOWN");
            errorResponse.put("error", "Health check failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
        }
    }

    @GetMapping("/redis/simple")
    @Operation(
        summary = "Simple Redis health check",
        description = "Performs a basic Redis health check using ping command. Returns a simple status response."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Redis is healthy",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = "{\"status\":\"UP\",\"healthy\":true}"))),
        @ApiResponse(responseCode = "503", description = "Redis is unhealthy",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = "{\"status\":\"DOWN\",\"healthy\":false,\"error\":\"Connection timeout\"}")))
    })
    public ResponseEntity<Map<String, Object>> simpleRedisHealth() {
        logger.info("Simple Redis health check requested");
        
        try {
            boolean isHealthy = redisHealthService.isHealthy();
            Map<String, Object> response = new HashMap<>();
            response.put("status", isHealthy ? "UP" : "DOWN");
            response.put("healthy", isHealthy);
            
            if (isHealthy) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
            }
        } catch (Exception e) {
            logger.error("Error performing simple Redis health check", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "DOWN");
            errorResponse.put("healthy", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
        }
    }
}