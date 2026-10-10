package com.programming.techie.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange
                        // Eureka UI
                        .pathMatchers("/eureka/**").permitAll()

                        // Health
                        .pathMatchers("/actuator/health", "/actuator/info").permitAll()

                        // Products: read public, write ADMIN
                        .pathMatchers(HttpMethod.GET, "/api/product", "/api/product/**").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/product", "/api/product/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.PUT, "/api/product", "/api/product/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/api/product", "/api/product/**").hasRole("ADMIN")

                        // Orders: USER or ADMIN
                        .pathMatchers(HttpMethod.POST, "/api/order", "/api/order/**").hasAnyRole("USER", "ADMIN")
                        .pathMatchers("/api/order/**").hasAnyRole("USER", "ADMIN")

                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                )
                .build();
    }

    private Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakRealmRoleConverter());
        return new ReactiveJwtAuthenticationConverterAdapter(converter);
    }
}