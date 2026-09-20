package com.programmingtechie.Product_services.integration;

import com.programmingtechie.Product_services.dto.ProductRequest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.web.client.RestClient;
import org.testcontainers.mongodb.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.web.client.HttpClientErrorException;
import org.junit.jupiter.params.ParameterizedTest;

import org.junit.jupiter.params.provider.CsvSource;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class ProductIntegrationTest {

    @Container
    @ServiceConnection
    static MongoDBContainer mongoDBContainer =
            new MongoDBContainer("mongo:8.0");

    @LocalServerPort
    private int port;

    @Test
    void createProduct_shouldReturnCreated() {

        ProductRequest request = new ProductRequest();
        request.setName("Laptop");
        request.setDescription("Gaming Laptop");
        request.setPrice(BigDecimal.valueOf(50000));

        RestClient restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();

        var response = restClient.post()
                .uri("/api/product")
                .body(request)
                .retrieve()
                .toBodilessEntity();

        assertThat(response.getStatusCode().is2xxSuccessful())
                .isTrue();
    }



    @Test
    void createProduct_shouldBeReturnedByGetAllProducts() {

        ProductRequest request = new ProductRequest();
        request.setName("Phone");
        request.setDescription("Smart Phone");
        request.setPrice(BigDecimal.valueOf(20000));

        RestClient restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();

        // Create product
        restClient.post()
                .uri("/api/product")
                .body(request)
                .retrieve()
                .toBodilessEntity();

        // Get all products
        var products = restClient.get()
                .uri("/api/product")
                .retrieve()
                .body(String.class);

        // Verify product exists in response
        assertThat(products)
                .contains("Phone")
                .contains("Smart Phone")
                .contains("20000");
    }





//
//    @ParameterizedTest
//    @ValueSource(strings = {"", "   "})
//    void createProduct_shouldReturnBadRequest_whenNameIsInvalid(String name) {
//
//        ProductRequest request = new ProductRequest();
//        request.setName(name);
//        request.setDescription("Gaming Laptop");
//        request.setPrice(BigDecimal.valueOf(50000));
//
//        RestClient restClient = RestClient.builder()
//                .baseUrl("http://localhost:" + port)
//                .build();
//
//        assertThatThrownBy(() ->
//                restClient.post()
//                        .uri("/api/product")
//                        .body(request)
//                        .retrieve()
//                        .toBodilessEntity()
//        )
//                .isInstanceOf(HttpClientErrorException.BadRequest.class);
//    }
//
//    @Test
//    void createProduct_shouldReturnBadRequest_whenDescriptionIsMissing() {
//
//        ProductRequest request = new ProductRequest();
//        request.setName("Laptop");
//        request.setPrice(BigDecimal.valueOf(50000));
//
//        RestClient restClient = RestClient.builder()
//                .baseUrl("http://localhost:" + port)
//                .build();
//
//        assertThatThrownBy(() ->
//                restClient.post()
//                        .uri("/api/product")
//                        .body(request)
//                        .retrieve()
//                        .toBodilessEntity()
//        )
//                .isInstanceOf(HttpClientErrorException.BadRequest.class);
//    }
//
//
//
//
//
//    @Test
//    void createProduct_shouldReturnBadRequest_whenPriceIsMissing() {
//
//        ProductRequest request = new ProductRequest();
//        request.setName("Laptop");
//        request.setDescription("Gaming Laptop");
//
//        RestClient restClient = RestClient.builder()
//                .baseUrl("http://localhost:" + port)
//                .build();
//
//        assertThatThrownBy(() ->
//                restClient.post()
//                        .uri("/api/product")
//                        .body(request)
//                        .retrieve()
//                        .toBodilessEntity()
//        )
//                .isInstanceOf(HttpClientErrorException.BadRequest.class);
//    }
//
//
//
//    @Test
//    void createProduct_shouldReturnBadRequest_whenPriceIsNegative() {
//
//        ProductRequest request = new ProductRequest();
//        request.setName("Laptop");
//        request.setDescription("Gaming Laptop");
//        request.setPrice(BigDecimal.valueOf(-500));
//
//        RestClient restClient = RestClient.builder()
//                .baseUrl("http://localhost:" + port)
//                .build();
//
//        assertThatThrownBy(() ->
//                restClient.post()
//                        .uri("/api/product")
//                        .body(request)
//                        .retrieve()
//                        .toBodilessEntity()
//        )
//                .isInstanceOf(HttpClientErrorException.BadRequest.class);
//    }



    @ParameterizedTest
    @CsvSource({
            "'', 'Gaming Laptop', '50000'",
            "'Laptop', '', '50000'",
            "'Laptop', 'Gaming Laptop', ''",
            "'Laptop', 'Gaming Laptop', '-500'"
    })
    void createProduct_shouldReturnBadRequest_whenRequestIsInvalid(
            String name,
            String description,
            String price) {

        ProductRequest request = new ProductRequest();
        request.setName(name);
        request.setDescription(description);

        if (price != null && !price.isBlank()) {
            request.setPrice(new BigDecimal(price));
        }

        RestClient restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();

        assertThatThrownBy(() ->
                restClient.post()
                        .uri("/api/product")
                        .body(request)
                        .retrieve()
                        .toBodilessEntity()
        )
                .isInstanceOf(HttpClientErrorException.BadRequest.class);
    }
    @Test
    void getProductById_shouldReturnNotFound_whenProductDoesNotExist() {

        RestClient restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();

        assertThatThrownBy(() ->
                restClient.get()
                        .uri("/api/product/not-found")
                        .retrieve()
                        .toBodilessEntity()
        )
                .isInstanceOf(HttpClientErrorException.NotFound.class);
    }

}