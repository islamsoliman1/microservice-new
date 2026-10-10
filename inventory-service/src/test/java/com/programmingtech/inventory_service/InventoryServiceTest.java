package com.programmingtech.inventory_service;

import com.programmingtech.inventory_service.dto.InventoryResponse;
import com.programmingtech.inventory_service.model.Inventory;
import com.programmingtech.inventory_service.repository.InventoryRepository;
import com.programmingtech.inventory_service.service.InventoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    InventoryRepository inventoryRepository;
    @InjectMocks
    InventoryService inventoryService;

    @Test
    void inStock_whenQuantityPositive() {
        Inventory inv = new Inventory();
        inv.setSkuCode("iphone_15");
        inv.setQuantity(5);
        when(inventoryRepository.findBySkuCodeIn(List.of("iphone_15")))
                .thenReturn(List.of(inv));

        List<InventoryResponse> result = inventoryService.isInStock(List.of("iphone_15"));

        assertThat(result.get(0).isInStock()).isTrue();
    }

    @Test
    void outOfStock_whenQuantityZero() {
        Inventory inv = new Inventory();
        inv.setSkuCode("iphone_13");
        inv.setQuantity(0);
        when(inventoryRepository.findBySkuCodeIn(List.of("iphone_13")))
                .thenReturn(List.of(inv));

        assertThat(inventoryService.isInStock(List.of("iphone_13")).get(0).isInStock())
                .isFalse();
    }
}
