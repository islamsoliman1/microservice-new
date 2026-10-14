package com.programming.techie.docs;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

@RestController
public class DocumentationController {
    private final WebClient client;
    private final OpenApiMerger merger;
    private final String overview;

    public DocumentationController(@Qualifier("openApiWebClient") WebClient.Builder builder,
                                   OpenApiMerger merger) throws IOException {
        this.client = builder.build();
        this.merger = merger;
        this.overview = new ClassPathResource("documentation-overview.md")
                .getContentAsString(StandardCharsets.UTF_8);
    }

    @GetMapping({"/docs", "/swagger-ui/index.html", "/webjars/swagger-ui/index.html"})
    public ResponseEntity<Void> documentation() {
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("/documentation/index.html")).build();
    }

    @GetMapping(value = "/openapi/all", produces = "application/json")
    public Mono<ObjectNode> combinedDefinition() {
        return Mono.zip(fetch("product-service"), fetch("order-service"), fetch("inventory-service"))
                .map(documents -> merger.merge(List.of(
                        new OpenApiMerger.Source("product", "Products", false, documents.getT1()),
                        new OpenApiMerger.Source("order", "Orders", false, documents.getT2()),
                        new OpenApiMerger.Source("inventory", "Inventory (Internal)", true, documents.getT3())),
                        overview))
                .onErrorMap(error -> new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                        "The complete API reference is unavailable. Check Product, Order, "
                                + "and Inventory service registration and OpenAPI endpoints.", error));
    }

    private Mono<JsonNode> fetch(String service) {
        return client.get().uri("http://" + service + "/v3/api-docs")
                .retrieve().bodyToMono(JsonNode.class)
                .switchIfEmpty(Mono.error(new IllegalStateException("Empty API definition: " + service)))
                .timeout(Duration.ofSeconds(10));
    }
}
