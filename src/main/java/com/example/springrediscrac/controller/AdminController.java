package com.example.springrediscrac.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.crac.CheckpointException;
import org.crac.Core;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@Tag(name = "CRaC Administration", description = "Java CRaC (Coordinated Restore at Checkpoint) administrative operations for performance optimization")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @PostMapping("/checkpoint")
    @Operation(
        summary = "Trigger CRaC checkpoint",
        description = "Initiates a CRaC checkpoint operation. The application will save its current state and terminate. Use with caution in production environments.",
        tags = {"CRaC Administration"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "500", description = "Checkpoint failed",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = "{\"status\":\"FAILED\",\"error\":\"Checkpoint failed: Connection refused\",\"exception\":\"CheckpointException\"}"))),
        @ApiResponse(responseCode = "501", description = "CRaC not supported in current environment",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = "{\"status\":\"UNSUPPORTED\",\"error\":\"CRaC checkpoint not supported in current environment\",\"message\":\"Make sure you're running with CRaC-enabled JDK\"}"))),
        @ApiResponse(responseCode = "200", description = "This response should not occur in normal operation",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = "{\"status\":\"UNEXPECTED_RETURN\",\"message\":\"Checkpoint returned unexpectedly\"}")))
    })
    public ResponseEntity<Map<String, Object>> triggerCheckpoint() {
        logger.info("Checkpoint trigger requested");
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            logger.info("Initiating CRaC checkpoint...");
            Core.checkpointRestore();
            
            // This line should not be reached in normal checkpoint flow
            response.put("status", "UNEXPECTED_RETURN");
            response.put("message", "Checkpoint returned unexpectedly");
            logger.warn("Checkpoint returned unexpectedly - this should not happen");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            
        } catch (CheckpointException e) {
            logger.error("Checkpoint failed", e);
            response.put("status", "FAILED");
            response.put("error", "Checkpoint failed: " + e.getMessage());
            response.put("exception", e.getClass().getSimpleName());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            
        } catch (UnsupportedOperationException e) {
            logger.warn("CRaC not supported in current environment", e);
            response.put("status", "UNSUPPORTED");
            response.put("error", "CRaC checkpoint not supported in current environment");
            response.put("message", "Make sure you're running with CRaC-enabled JDK");
            
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(response);
            
        } catch (Exception e) {
            logger.error("Unexpected error during checkpoint", e);
            response.put("status", "ERROR");
            response.put("error", "Unexpected error: " + e.getMessage());
            response.put("exception", e.getClass().getSimpleName());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}