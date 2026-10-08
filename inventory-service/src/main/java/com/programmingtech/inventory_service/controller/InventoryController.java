package com.programmingtech.inventory_service.controller;


import com.programmingtech.inventory_service.dto.InventoryResponse;
import com.programmingtech.inventory_service.model.Inventory;
import com.programmingtech.inventory_service.service.InventoryService;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Validated
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<InventoryResponse> isInStock(
            @RequestParam
            @NotEmpty(message = "At least one skuCode is required")
            List<String> skuCode) {
        return inventoryService.isInStock(skuCode);
    }
}
