package com.programmingtechie.Product_services.service;


import com.programmingtechie.Product_services.dto.ProductRequest;
import com.programmingtechie.Product_services.dto.ProductResponse;
import com.programmingtechie.Product_services.model.Product;
import com.programmingtechie.Product_services.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.programmingtechie.Product_services.exception.ProductNotFoundException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;



    public void createProduct(ProductRequest productRequest){


        Product product=Product.builder().name(productRequest.getName()).description(productRequest.getDescription()).price(productRequest.getPrice()).build();

          productRepository.save(product);
          log.info("product is saved");

    }


public List<ProductResponse> getAllproduct(){

        List<Product> products =productRepository.findAll();

      return   products.stream().map(this::mapToProductResponse).toList();

}


private ProductResponse mapToProductResponse(Product product){

    return ProductResponse.builder()
            .id(product.getId())
            .name(product.getName())
            .description(product.getDescription())
            .price(product.getPrice())
            .build();
    }

    public ProductResponse getProductById(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        return mapToProductResponse(product);
    }

}

