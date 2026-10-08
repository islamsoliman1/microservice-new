package com.programmingtechie.order_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI orderServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Order Service API")
                        .description("Place orders, check inventory via Eureka, publish order-placed events to Kafka")
                        .version("1.0.0")
                        .contact(new Contact().name("Islam").url("https://github.com/islamsoliman1"))
                        .license(new License().name("MIT")));
    }
}