package com.example.analytics.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().info(new Info()
                .title("Library Analytics Service API")
                .version("v1")
                .description("Kafka consumer + Redis caching + Elasticsearch indexing/search + Postgres native SQL reports."));
    }
}
