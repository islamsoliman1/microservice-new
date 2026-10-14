
package com.programmingtechie.order_service.controller;

import com.programmingtechie.order_service.dto.OrderRequest;
import com.programmingtechie.order_service.service.OrderService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
        name = "Orders",
        description = "Order placement"
)
public class OrderController {

    private final OrderService orderService;

    @Operation(
            operationId = "placeOrder",
            summary = "Place a new order",
            description = "Requires USER or ADMIN at the gateway. Use an existing inventory SKU. "
                    + "The current demo does not reserve stock or verify catalog prices. "
                    + "The fallback also returns 201: inspect the response body, not just the status.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Success message, or a failure message from the current fallback",
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"),
                            examples = {
                                    @ExampleObject(name = "Success", value = "Order Placed Success"),
                                    @ExampleObject(name = "Fallback", value = "Oops something went wrong. Try again later")
                            })
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid order request",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing or invalid access token at the gateway",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Token does not have USER or ADMIN role",
                    content = @Content
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



