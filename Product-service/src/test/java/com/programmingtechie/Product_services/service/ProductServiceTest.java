package com.programmingtechie.Product_services.service;

import com.programmingtechie.Product_services.dto.ProductRequest;
import com.programmingtechie.Product_services.exception.ProductNotFoundException;
import com.programmingtechie.Product_services.model.Product;
import com.programmingtechie.Product_services.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    ProductRepository productRepository;
    @InjectMocks
    ProductService productService;

    @Test
    void createProduct_savesMappedEntity() {
        ProductRequest request = ProductRequest.builder()
                .name("iPhone 15")
                .description("Latest")
                .price(new BigDecimal("999.99"))
                .build();

        productService.createProduct(request);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("iPhone 15");
    }

    @Test
    void getProductById_notFound() {
        when(productRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById("missing"))
                .isInstanceOf(ProductNotFoundException.class);
    }
}
