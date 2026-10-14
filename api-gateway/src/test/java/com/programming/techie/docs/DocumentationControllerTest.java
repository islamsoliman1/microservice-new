package com.programming.techie.docs;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import tools.jackson.databind.json.JsonMapper;
import static org.junit.jupiter.api.Assertions.*;

class DocumentationControllerTest {
    @Test
    void returns503RatherThanAPartialReferenceWhenAServiceIsUnavailable() throws Exception {
        WebClient.Builder builder = WebClient.builder().exchangeFunction(request ->
                Mono.just(ClientResponse.create(HttpStatus.SERVICE_UNAVAILABLE).build()));
        DocumentationController controller = new DocumentationController(
                builder, new OpenApiMerger(new JsonMapper()));
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> controller.combinedDefinition().block());
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, error.getStatusCode());
    }

    @Test
    void rejectsAnEmptyUpstreamDocument() throws Exception {
        WebClient.Builder builder = WebClient.builder().exchangeFunction(request ->
                Mono.just(ClientResponse.create(HttpStatus.OK).build()));
        DocumentationController controller = new DocumentationController(
                builder, new OpenApiMerger(new JsonMapper()));
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> controller.combinedDefinition().block());
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, error.getStatusCode());
    }
}
