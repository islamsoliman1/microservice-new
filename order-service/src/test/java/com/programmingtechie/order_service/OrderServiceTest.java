package com.programmingtechie.order_service;


import com.programmingtech.inventory_service.exception.InventoryServiceException;
import com.programmingtechie.order_service.dto.OrderLineItemDto;
import com.programmingtechie.order_service.dto.OrderRequest;
import com.programmingtechie.order_service.event.OrderPlacedEvent;
import com.programmingtechie.order_service.exception.InsufficientStockException;
import com.programmingtechie.order_service.model.Order;
import com.programmingtechie.order_service.repository.OrderRepository;
import com.programmingtechie.order_service.service.OrderService;
import io.micrometer.tracing.Tracer;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    OrderRepository orderRepository;

    @Mock
    ObjectProvider<Tracer> tracerProvider;

    @Mock
    KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    MockWebServer mockWebServer;
    OrderService orderService;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        when(tracerProvider.getIfAvailable()).thenReturn(null);

        orderService = new OrderService(
                orderRepository,
                WebClient.builder(),
                tracerProvider,
                kafkaTemplate
        );

        ReflectionTestUtils.setField(
                orderService,
                "inventoryServiceUrl",
                mockWebServer.url("/").toString().replaceAll("/$", "")
        );
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void placeOrder_success() {
        mockWebServer.enqueue(new MockResponse()
                .setBody("[{\"skuCode\":\"iphone_15\",\"inStock\":true}]")
                .addHeader("Content-Type", "application/json"));

        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        orderService.placeOrder(sampleRequest("iphone_15"));

        verify(orderRepository).save(any(Order.class));
        verify(kafkaTemplate).send(eq("order-placed"), any(OrderPlacedEvent.class));
    }

    @Test
    void placeOrder_outOfStock() {
        mockWebServer.enqueue(new MockResponse()
                .setBody("[{\"skuCode\":\"iphone_15\",\"inStock\":false}]")
                .addHeader("Content-Type", "application/json"));

        assertThatThrownBy(() -> orderService.placeOrder(sampleRequest("iphone_15")))
                .isInstanceOf(InsufficientStockException.class);

        // مهم: مش المفروض يحصل save لما مفيش stock
        verify(orderRepository, never()).save(any());
        verify(kafkaTemplate, never()).send(any(), any());
    }

    @Test
    void placeOrder_inventoryHttpError() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500).setBody("error"));

        assertThatThrownBy(() -> orderService.placeOrder(sampleRequest("iphone_15")))
                .isInstanceOf(InventoryServiceException.class);

        verify(orderRepository, never()).save(any());
    }

    private OrderRequest sampleRequest(String sku) {
        OrderLineItemDto item = new OrderLineItemDto();
        item.setSkuCode(sku);
        item.setPrice(new BigDecimal("10.00"));
        item.setQuantity(1);

        OrderRequest req = new OrderRequest();
        req.setOrderLineItemsDtoList(List.of(item));
        return req;
    }
}