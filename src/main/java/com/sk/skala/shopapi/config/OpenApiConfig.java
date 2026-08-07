package com.sk.skala.shopapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI skalaShopOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("SKALA-SHOP API")
                .version("1.0")
                .description("SKALA 온라인 쇼핑몰 백엔드 REST API"));
    }
}
