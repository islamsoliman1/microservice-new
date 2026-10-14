
package com.programmingtechie.order_service.dto;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderLineItemDto {

    @Schema(hidden = true)
    private long id;

    @NotBlank(message = "SKU code is required")
    @Schema(example = "iphone_15", description = "Example only; replace with an existing inventory SKU.")
    private String skuCode;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    @Schema(example = "999.99", description = "Client-supplied in this demo; not verified against the catalog.")
    private BigDecimal price;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than zero")
    @Schema(example = "1", description = "Positive quantity; the current implementation does not reserve stock.")
    private Integer quantity;
}


