package com.programmingtechie.Product_services.config;

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
    public OpenAPI productServiceOpenAPI() {
        return new OpenAPI()
                // Relative to the documentation origin, so gateway requests stay on the gateway.
                .addServersItem(new Server().url("/").description("Current origin"))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP)
                                .scheme("bearer").bearerFormat("JWT")
                                .description("Keycloak access token. Paste the token without the Bearer prefix. "
                                        + "See docs/api-demo.md for local token instructions.")))
                .info(new Info()
                        .title("Product Service API")
                        .description("Product catalog backed by MongoDB. Reads are public through the gateway; creating a product requires the ADMIN realm role. Authorization is enforced at the gateway, not on the direct service port.")
                        .version("1.0.0")
                        .contact(new Contact().name("Islam").url("https://github.com/islamsoliman1")));
    }
}
