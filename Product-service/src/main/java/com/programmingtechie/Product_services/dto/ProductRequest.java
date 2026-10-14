package com.programmingtechie.Product_services.dto;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductRequest {

        @NotBlank(message = "Product name is required")
        @Schema(example = "Wireless Keyboard")
        private String name;

        @NotBlank(message = "Product description is required")
        @Schema(example = "Compact USB-C rechargeable keyboard")
        private String description;

        @NotNull(message = "Product price is required")
        @Positive(message = "Product price must be positive")
        @Schema(example = "49.99")
        private BigDecimal price;


}
