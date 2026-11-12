package com.project.orderservice.function;

import com.project.orderservice.dto.StockRejectedEvent;
import com.project.orderservice.dto.StockReservedEvent;
import com.project.orderservice.model.Order;
import com.project.orderservice.model.OrderStatus;
import com.project.orderservice.model.ProcessedEvent;
import com.project.orderservice.repository.OrderRepository;
import com.project.orderservice.repository.ProcessedEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class OrderResponseConsumerTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProcessedEventRepository processedEventRepository;

    @InjectMocks
    private OrderResponseConsumer orderResponseConsumer;

    @Test
    void whenStockReserved_ThenUpdateStatusToConfirmed() {
        StockReservedEvent event = new StockReservedEvent(1L, "LAPTOP", 2);
        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.CREATED);

        String expectedId = OrderStatus.CONFIRMED.name() + "-1";

        when(processedEventRepository.existsById(expectedId)).thenReturn(false);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        orderResponseConsumer.handleStockReservedEvent().accept(event);

        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
        verify(orderRepository, times(1)).save(order);
        verify(processedEventRepository, times(1)).save(any(ProcessedEvent.class));
    }

    @Test
    void whenStockReserved_ButEventIsDuplicate_ThenDoNothing() {
        StockReservedEvent event = new StockReservedEvent(1L, "LAPTOP", 2);

        String expectedId = OrderStatus.CONFIRMED.name() + "-1";

        when(processedEventRepository.existsById(expectedId)).thenReturn(true);

        orderResponseConsumer.handleStockReservedEvent().accept(event);

        verify(orderRepository, never()).findById(any());
        verify(orderRepository, never()).save(any());
        verify(processedEventRepository, never()).save(any());
    }

    @Test
    void whenStockRejected_ThenUpdateStatusToRejected() {
        StockRejectedEvent event = new StockRejectedEvent(2L, "LAPTOP", "Insufficient stock");
        Order order = new Order();
        order.setId(2L);
        order.setStatus(OrderStatus.CREATED);

        when(processedEventRepository.existsById("REJECTED-2")).thenReturn(false);
        when(orderRepository.findById(2L)).thenReturn(Optional.of(order));

        orderResponseConsumer.handleStockRejectedEvent().accept(event);

        assertEquals(OrderStatus.REJECTED, order.getStatus());
        verify(orderRepository, times(1)).save(order);
        verify(processedEventRepository, times(1)).save(any(ProcessedEvent.class));
    }
}
