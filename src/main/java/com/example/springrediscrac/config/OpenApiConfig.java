package com.example.springrediscrac.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private int serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Spring Boot + CRaC + Redis API")
                        .description("A production-ready Spring Boot 3.4.6 application demonstrating Java CRaC (Coordinated Restore at Checkpoint) functionality with Redis integration. This API provides comprehensive cache operations, health monitoring, and CRaC checkpoint management.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Mrutyunjay Patil")
                                .email("patilmrutyunjay2@gmail.com")
                                .url("https://mrutyunjaypatil.dev"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Local Development Server")))
                .tags(List.of(
                        new Tag()
                                .name("Cache Operations")
                                .description("Redis cache management operations including CRUD operations, TTL management, and bulk operations"),
                        new Tag()
                                .name("Health Monitoring")
                                .description("Application and Redis health check endpoints for monitoring service availability"),
                        new Tag()
                                .name("CRaC Administration")
                                .description("Java CRaC (Coordinated Restore at Checkpoint) administrative operations for performance optimization")));
    }
}