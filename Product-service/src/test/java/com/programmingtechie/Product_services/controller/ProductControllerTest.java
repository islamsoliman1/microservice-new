package com.programmingtechie.Product_services.controller;



import tools.jackson.databind.ObjectMapper;
import com.programmingtechie.Product_services.dto.ProductRequest;
import com.programmingtechie.Product_services.dto.ProductResponse;
import com.programmingtechie.Product_services.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.mockito.Mockito.never;

@WebMvcTest(ProductController.class)
public class ProductControllerTest {




    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;


    @Test
    void createProduct_shouldReturnCreated() throws Exception {

        ProductRequest request = new ProductRequest();

        request.setName("Laptop");
        request.setDescription("Gaming Laptop");
        request.setPrice(BigDecimal.valueOf(50000));

        mockMvc.perform(post("/api/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(productService).createProduct(any(ProductRequest.class));
    }




    @Test
    void getAllProducts_shouldReturnProducts() throws Exception {

        ProductResponse product = ProductResponse.builder()
                .id("123")
                .name("Laptop")
                .description("Gaming Laptop")
                .price(BigDecimal.valueOf(50000))
                .build();

        when(productService.getAllProducts())
                .thenReturn(List.of(product));

        mockMvc.perform(get("/api/product"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("123"))
                .andExpect(jsonPath("$[0].name").value("Laptop"))
                .andExpect(jsonPath("$[0].description").value("Gaming Laptop"))
                .andExpect(jsonPath("$[0].price").value(50000));

        verify(productService).getAllProducts();
    }




    @Test
    void createProduct_shouldReturnBadRequest_whenNameIsMissing() throws Exception {

        ProductRequest request = new ProductRequest();
        request.setDescription("Gaming Laptop");
        request.setPrice(BigDecimal.valueOf(50000));

        mockMvc.perform(
                        post("/api/product")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
              verify(productService, never())
                .createProduct(any(ProductRequest.class));


    }



    @Test
    void createProduct_shouldReturnBadRequest_whenDescriptionIsMissing() throws Exception {

        ProductRequest request = new ProductRequest();
        request.setName("Laptop");
        request.setPrice(BigDecimal.valueOf(50000));

        mockMvc.perform(
                        post("/api/product")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
        verify(productService, never())
                .createProduct(any(ProductRequest.class));



    }



    @Test
    void createProduct_shouldReturnBadRequest_whenPriceIsMissing() throws Exception {

        ProductRequest request = new ProductRequest();
        request.setName("Laptop");
        request.setDescription("Gaming Laptop");

        mockMvc.perform(
                        post("/api/product")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
        verify(productService, never())
                .createProduct(any(ProductRequest.class));

    }



    @Test
    void createProduct_shouldReturnBadRequest_whenPriceIsNegative() throws Exception {

        ProductRequest request = new ProductRequest();
        request.setName("Laptop");
        request.setDescription("Gaming Laptop");
        request.setPrice(BigDecimal.valueOf(-100));

        mockMvc.perform(
                        post("/api/product")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
        verify(productService, never())
                .createProduct(any(ProductRequest.class));


    }






}
