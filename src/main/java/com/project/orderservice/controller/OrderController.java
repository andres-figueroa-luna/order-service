package com.project.orderservice.controller;

import com.project.orderservice.dto.*;
import com.project.orderservice.model.Order;
import com.project.orderservice.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public OrderCreatedResponse createOrder(@RequestBody @Valid OrderRequest request) {
        return orderService.createOrder(request);
    }

    @GetMapping("/{id}")
    public OrderResponse getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }

    @GetMapping
    public PageResponse<OrderResponse> getAllOrders(@RequestParam(required = false) String status, Pageable pageable) {
        return orderService.getAllOrders(status, pageable);
    }

    @PatchMapping("/{id}/cancel")
    public OrderActionResponse cancelOrder(@PathVariable Long id) {
        return orderService.cancelOrder(id);
    }
}
