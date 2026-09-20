package com.programmingtech.inventory_service.repository;

import com.programmingtech.inventory_service.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface  InventoryRepository extends JpaRepository<Inventory,Long> {


    Optional<Inventory> findById(Long aLong);




    List<Inventory> findBySkuCodeIn(List<String> skuCode);
}
