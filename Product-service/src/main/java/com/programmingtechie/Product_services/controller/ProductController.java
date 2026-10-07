package com.programmingtechie.Product_services.controller;



import com.programmingtechie.Product_services.dto.ProductRequest;
import com.programmingtechie.Product_services.dto.ProductResponse;
import com.programmingtechie.Product_services.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Products",
        description = "APIs for managing products"
)
@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;


    @Operation(
            summary = "Create a product",
            description = "Creates a new product in the product catalog"
    )
    @ApiResponse(
            responseCode = "201",
            description = "Product created successfully"
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createProduct(
            @Valid @RequestBody ProductRequest productRequest) {

        productService.createProduct(productRequest);
    }


    @Operation(
            summary = "Get all products",
            description = "Returns all products from the product catalog"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Products retrieved successfully"
    )
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ProductResponse> getAllProducts() {

        return productService.getAllProducts();
    }


    @Operation(
            summary = "Get product by ID",
            description = "Returns a product using its unique ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Product found"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Product not found"
            )
    })
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ProductResponse getProductById(
            @PathVariable String id) {

        return productService.getProductById(id);
    }
}
