package com.programmingtechie.Product_services.controller;



import com.programmingtechie.Product_services.dto.ProductRequest;
import com.programmingtechie.Product_services.dto.ProductResponse;
import com.programmingtechie.Product_services.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
            operationId = "createProduct",
            summary = "Create a product",
            description = "Creates a product. Requires an ADMIN access token at the gateway.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Product created; empty response body", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid product fields", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token at the gateway", content = @Content),
            @ApiResponse(responseCode = "403", description = "Token does not have ADMIN role", content = @Content)
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createProduct(
            @Valid @RequestBody ProductRequest productRequest) {

        productService.createProduct(productRequest);
    }


    @Operation(
            operationId = "listProducts",
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
            operationId = "getProduct",
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
                    description = "Product not found",
                    content = @Content
            )
    })
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ProductResponse getProductById(
            @PathVariable String id) {

        return productService.getProductById(id);
    }
}
