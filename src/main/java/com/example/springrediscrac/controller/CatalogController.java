package com.example.springrediscrac.controller;

import com.example.springrediscrac.service.CatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/catalog")
@Tag(name = "Catalog Cache Operations", description = "Spring Cache annotation based operations for catalog data")
public class CatalogController {

    @Autowired
    private CatalogService catalogService;

    @GetMapping("/{id}")
    @Operation(
        summary = "Get cached catalog data",
        description = "Retrieves catalog data using Spring's @Cacheable annotation. First call will be slow (cache miss), subsequent calls will be fast (cache hit)."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Catalog data retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error occurred")
    })
    public String getFromCache(@Parameter(description = "Catalog item ID", example = "item123") @PathVariable String id) {
        return catalogService.getCachedData(id);
    }

    @PostMapping("/{id}")
    @Operation(
        summary = "Update cached catalog data",
        description = "Updates catalog data using Spring's @CachePut annotation. This will update both the cache and return the data."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cache updated successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error occurred")
    })
    public String putToCache(
        @Parameter(description = "Catalog item ID", example = "item123") @PathVariable String id, 
        @Parameter(description = "Data to cache") @RequestBody String data) {
        return catalogService.updateCache(id, data);
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Evict cached catalog data",
        description = "Removes specific catalog data from cache using Spring's @CacheEvict annotation."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cache evicted successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error occurred")
    })
    public String evictFromCache(@Parameter(description = "Catalog item ID", example = "item123") @PathVariable String id) {
        catalogService.evictCache(id);
        return "Cache evicted for id: " + id;
    }

    @DeleteMapping("/all")
    @Operation(
        summary = "Evict all cached catalog data",
        description = "Removes all catalog data from cache using Spring's @CacheEvict(allEntries=true) annotation."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "All cache entries evicted successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error occurred")
    })
    public String evictAllCache() {
        catalogService.evictAllCache();
        return "All cache entries evicted";
    }
}