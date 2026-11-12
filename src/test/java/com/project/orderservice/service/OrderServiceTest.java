package com.project.orderservice.service;

import com.project.orderservice.dto.OrderCreatedResponse;
import com.project.orderservice.dto.OrderRequest;
import com.project.orderservice.exception.BusinessRuleException;
import com.project.orderservice.exception.ResourceNotFoundException;
import com.project.orderservice.model.Order;
import com.project.orderservice.model.OrderStatus;
import com.project.orderservice.publisher.KafkaEventPublisher;
import com.project.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private KafkaEventPublisher eventPublisher;

    @InjectMocks
    private OrderService orderService;

    @Test
    void whenCreateOrder_ThenSaveToDbAndPublishEvent() {
        OrderRequest request = new OrderRequest("LAPTOP-ROG-001", 2);

        Order savedOrder = new Order();
        savedOrder.setId(1L);
        savedOrder.setSku("LAPTOP-ROG-001");
        savedOrder.setQuantity(2);
        savedOrder.setStatus(OrderStatus.CREATED);

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(eventPublisher.publishEvent(anyString(), any())).thenReturn(CompletableFuture.completedFuture(null));

        OrderCreatedResponse response = orderService.createOrder(request);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("LAPTOP-ROG-001", response.sku());

        verify(orderRepository, times(1)).save(any(Order.class));
        verify(eventPublisher, times(1)).publishEvent(eq("orderCreated-out-0"), any());
    }

    @Test
    void whenCancelRejectedOrder_ThenThrowBusinessRuleException() {
        Order rejectedOrder = new Order();
        rejectedOrder.setId(1L);
        rejectedOrder.setStatus(OrderStatus.REJECTED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(rejectedOrder));

        BusinessRuleException exception = assertThrows(BusinessRuleException.class,
                () -> orderService.cancelOrder(1L));

        assertEquals("Cannot cancel order with status: REJECTED", exception.getMessage());

        verify(orderRepository, never()).save(any(Order.class));
        verify(eventPublisher, never()).publishEvent(anyString(), any());
    }

    @Test
    void whenGetOrderById_ButNotExists_ThenThrowResourceNotFoundException() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> orderService.getOrderById(99L));

        assertEquals("Order with id 99 not found", exception.getMessage());
    }
}
