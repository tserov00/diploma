package org.example.orderservice.controller;

import jakarta.validation.Valid;
import org.example.orderservice.dto.request.OrderDto;
import org.example.orderservice.dto.response.OrderResponseDto;
import org.example.orderservice.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<Void> createOrder(@Valid @RequestBody OrderDto orderDto, @RequestHeader("User-Id") Long userId) {
        orderService.createOrder(orderDto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long id, @RequestHeader("User-Id") Long userId) {
        orderService.cancelOrder(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/history")
    public ResponseEntity<List<OrderResponseDto>> getHistory(@RequestHeader("User-Id") Long userId) {
        return ResponseEntity.ok(orderService.getHistory(userId));
    }
}
