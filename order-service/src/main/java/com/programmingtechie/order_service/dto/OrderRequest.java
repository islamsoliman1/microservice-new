package com.programmingtechie.order_service.dto;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {

    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    @ArraySchema(minItems = 1, arraySchema = @Schema(description = "At least one item. The field name is orderLineItemsDtoList."))
    private List<OrderLineItemDto> orderLineItemsDtoList;



}







