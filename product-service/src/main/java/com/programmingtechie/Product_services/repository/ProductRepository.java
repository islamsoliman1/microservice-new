package com.programmingtechie.Product_services.repository;

import com.programmingtechie.Product_services.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductRepository extends MongoRepository<Product, String> {

}
