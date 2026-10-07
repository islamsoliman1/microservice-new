
package com.programmingtechie.order_service.controller;

import com.programmingtechie.order_service.dto.OrderRequest;
import com.programmingtechie.order_service.service.OrderService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
@Tag(
        name = "Order Controller",
        description = "APIs for managing orders"
)
public class OrderController {

    private final OrderService orderService;

    @Operation(
            summary = "Place a new order",
            description = "Creates a new order after checking inventory availability"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Order placed successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid order request"
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "Inventory service unavailable"
            )
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @CircuitBreaker(
            name = "inventory",
            fallbackMethod = "fallbackMethod"
    )
    @TimeLimiter(name = "inventory")
    @Retry(name = "inventory")
    public CompletableFuture<String> placeOrder(
          @Valid @RequestBody OrderRequest orderRequest
    ) {

        orderService.placeOrder(orderRequest);

        return CompletableFuture.supplyAsync(
                () -> "Order Placed Success"
        );
    }

    public CompletableFuture<String> fallbackMethod(
            OrderRequest orderRequest,
            RuntimeException runtimeException
    ) {

        return CompletableFuture.supplyAsync(
                () -> "Oops something went wrong. Try again later"
        );
    }
}



