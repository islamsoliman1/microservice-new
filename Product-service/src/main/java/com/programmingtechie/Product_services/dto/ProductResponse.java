package com.programmingtechie.Product_services.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Data
@Builder

@AllArgsConstructor
@NoArgsConstructor
public class ProductResponse {


    @Schema(example = "507f1f77bcf86cd799439011")
    private String id;

    @Schema(example = "Wireless Keyboard")
    private String name;

    @Schema(example = "Compact USB-C rechargeable keyboard")
    private String description;

    @Schema(example = "49.99")
    private BigDecimal price;



}
