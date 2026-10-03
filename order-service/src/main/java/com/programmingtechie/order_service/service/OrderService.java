package com.programmingtechie.order_service.service;


import com.programmingtechie.order_service.dto.InventoryResponse;
import com.programmingtechie.order_service.dto.OrderLineItemDto;
import com.programmingtechie.order_service.dto.OrderRequest;
import com.programmingtechie.order_service.event.OrderPlacedEvent;
import com.programmingtechie.order_service.model.Order;
import com.programmingtechie.order_service.model.OrderLineItems;
import com.programmingtechie.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.Span;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;


@RequiredArgsConstructor
@Transactional
@Slf4j
@Service
public class OrderService {


   private final OrderRepository orderRepository;
   private final WebClient.Builder webClientBuilder;
private final Tracer tracer;
private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;


    public void placeOrder(OrderRequest orderRequest) {

        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(this::mapToDto)
                .toList();
        order.setOrderLineItemsList(orderLineItems);

        List<String> skuCodes = order.getOrderLineItemsList().stream()
                .map(OrderLineItems::getSkuCode)
                .toList();

        log.info("Calling inventory Service");
        Span inventoryServiceLookup = tracer.nextSpan().name("InventoryServiceLookup");

        try (Tracer.SpanInScope ws = tracer.withSpan(inventoryServiceLookup.start())) {

            InventoryResponse[] inventoryResponseArray = webClientBuilder.build()
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("http")
                            .host("localhost")
                            .port(8082)
                            .path("/api/inventory")
                            .queryParam("skuCode", skuCodes)
                            .build())
                    .retrieve()
                    .bodyToMono(InventoryResponse[].class)
                    .block();

            if (inventoryResponseArray == null) {
                throw new IllegalStateException("Inventory service returned no response");
            }

            boolean allProductInStock = Arrays.stream(inventoryResponseArray)
                    .allMatch(InventoryResponse::isInStock);

            if (allProductInStock) {
                orderRepository.save(order);
                kafkaTemplate.send("NotificationTopic", new OrderPlacedEvent(order.getOrderNumber()));
            } else {
                throw new IllegalArgumentException("Product is not in stock");
            }
        } finally {
            inventoryServiceLookup.end();
        }
    }

    private OrderLineItems mapToDto(OrderLineItemDto orderLineItemDto){

        OrderLineItems orderLineItems =new OrderLineItems();
        orderLineItems.setPrice(orderLineItemDto.getPrice());
        orderLineItems.setQuantity(orderLineItemDto.getQuantity());
        orderLineItems.setSkuCode(orderLineItemDto.getSkuCode());
        return orderLineItems;


    }

}




