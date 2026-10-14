package com.programmingtech.inventory_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI inventoryServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Inventory Service API")
                        .description("Internal SKU stock lookup backed by SQL Server. Reference only in the "
                                + "combined gateway documentation; no gateway business route is exposed.")
                        .version("1.0.0")
                        .contact(new Contact().name("Islam").url("https://github.com/islamsoliman1")));
    }
}
