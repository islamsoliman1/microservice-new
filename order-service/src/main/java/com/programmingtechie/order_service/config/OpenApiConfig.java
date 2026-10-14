package com.programmingtechie.order_service.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI orderServiceOpenAPI() {
        return new OpenAPI()
                // Relative to the documentation origin, so gateway requests stay on the gateway.
                .addServersItem(new Server().url("/").description("Current origin"))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP)
                                .scheme("bearer").bearerFormat("JWT")
                                .description("Keycloak access token. Paste the token without the Bearer prefix. "
                                        + "See docs/api-demo.md for local token instructions.")))
                .info(new Info()
                        .title("Order Service API")
                        .description("Order placement backed by SQL Server with inventory lookup and Kafka publication to notificationTopic. Requires USER or ADMIN through the gateway. Demo limitations: prices come from the request, stock is not reserved, and a fallback can return HTTP 201 with a failure message. Not a production checkout.")
                        .version("1.0.0")
                        .contact(new Contact().name("Islam").url("https://github.com/islamsoliman1")));
    }
}
