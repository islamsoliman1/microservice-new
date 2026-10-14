package com.programmingtech.inventory_service.controller;


import com.programmingtech.inventory_service.dto.InventoryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Inventory", description = "Internal SKU stock lookup; no reservation or quantity allocation")
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    @Operation(operationId = "checkStock", summary = "Check stock by SKU",
            description = "Internal lookup used by Order Service. Returns existing SKUs with quantity greater "
                    + "than zero as in stock; unknown SKUs are omitted. Includes a deliberate delay.")
    @ResponseStatus(HttpStatus.OK)
    public List<InventoryResponse> isInStock(
            @RequestParam
            @Parameter(description = "One or more existing inventory SKUs",
                    array = @ArraySchema(schema = @Schema(type = "string", example = "iphone_15")))
            @NotEmpty(message = "At least one skuCode is required")
            List<String> skuCode) {
        return inventoryService.isInStock(skuCode);
    }
}
