package com.programmingtechie.order_service.dto;

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

    private long id;
    private String skuCode;
    private BigDecimal price;
    private Integer quantity;
}