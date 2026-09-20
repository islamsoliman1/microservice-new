package com.programmingtechie.Product_services.controller;


import com.programmingtechie.Product_services.dto.ProductRequest;
import com.programmingtechie.Product_services.dto.ProductResponse;
import com.programmingtechie.Product_services.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

private final ProductService productService;



@PostMapping
@ResponseStatus(HttpStatus.CREATED)
public void createProduct(@Valid @RequestBody ProductRequest productRequest){

        productService.createProduct(productRequest);

}


@GetMapping
@ResponseStatus(HttpStatus.OK)
public  List<ProductResponse> getAllproducts(){
  return productService.getAllproduct();

}


    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ProductResponse getProductById(@PathVariable String id) {
        return productService.getProductById(id);
    }

}
