package com.programmingtechie.Product_services.repository;


import com.programmingtechie.Product_services.model.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.mongodb.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;







@DataMongoTest
@Testcontainers
public class ProductRepositoryTest {




    @Container
    @ServiceConnection
    static MongoDBContainer mongoDBContainer =
            new MongoDBContainer("mongo:8.0");

    @Autowired
    private ProductRepository productRepository;

    @Test
    void saveProduct_shouldSaveProduct() {

        Product product = Product.builder()
                .name("Laptop")
                .description("Gaming Laptop")
                .price(BigDecimal.valueOf(50000))
                .build();

        Product savedProduct = productRepository.save(product);

        assertThat(savedProduct).isNotNull();
        assertThat(savedProduct.getId()).isNotNull();
        assertThat(savedProduct.getName()).isEqualTo("Laptop");
        assertThat(savedProduct.getDescription())
                .isEqualTo("Gaming Laptop");
        assertThat(savedProduct.getPrice())
                .isEqualByComparingTo(BigDecimal.valueOf(50000));
    }





    @Test
    void findAll_shouldReturnAllProducts() {

        Product laptop = Product.builder()
                .name("Laptop")
                .description("Gaming Laptop")
                .price(BigDecimal.valueOf(50000))
                .build();

        Product phone = Product.builder()
                .name("Phone")
                .description("Smart Phone")
                .price(BigDecimal.valueOf(20000))
                .build();

        productRepository.save(laptop);
        productRepository.save(phone);

        List<Product> products = productRepository.findAll();

        assertThat(products).hasSize(2);

        assertThat(products)
                .extracting(Product::getName)
                .containsExactlyInAnyOrder("Laptop", "Phone");
    }


    @Test
    void findAll_shouldReturnEmptyList_whenNoProductsExist() {

        List<Product> products = productRepository.findAll();

        assertThat(products).isEmpty();
    }




}
