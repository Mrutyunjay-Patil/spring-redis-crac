package com.example.springrediscrac.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.LocalDateTime;

@Schema(description = "Cache item containing key, value, and timestamps")
public class CacheItem implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Schema(description = "The cache key identifier", example = "user:123", required = true)
    @NotBlank(message = "Key cannot be blank")
    private String key;
    
    @Schema(description = "The cached value (can be any JSON-serializable type)", example = "John Doe")
    private Object value;
    
    @Schema(description = "Timestamp when the item was created", example = "2024-01-01T10:00:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "Timestamp when the item was last updated", example = "2024-01-01T10:30:00")
    private LocalDateTime updatedAt;

    public CacheItem() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @JsonCreator
    public CacheItem(@JsonProperty("key") String key, @JsonProperty("value") Object value) {
        this.key = key;
        this.value = value;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "CacheItem{" +
                "key='" + key + '\'' +
                ", value=" + value +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}