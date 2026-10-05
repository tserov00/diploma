package org.example.gatewayservice.controller;

import jakarta.validation.Valid;
import org.example.gatewayservice.dto.request.OrderDto;
import org.example.gatewayservice.dto.response.OrderResponseDto;
import org.example.gatewayservice.security.UserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class GatewayOrderController {
    private final WebClient orderWebClient;

    public GatewayOrderController(WebClient orderWebClient) {
        this.orderWebClient = orderWebClient;
    }

    @PostMapping
    public ResponseEntity<byte[]> createOrder(@Valid @RequestBody OrderDto order, @AuthenticationPrincipal UserPrincipal user) {
        ResponseEntity<byte[]> downstream = orderWebClient.post()
                .uri("/api/orders")
                .header("User-Id", String.valueOf(user.getUserId()))
                .bodyValue(order)
                .exchangeToMono(response -> response.toEntity(byte[].class))
                .block();

        if (downstream == null) return ResponseEntity.internalServerError().build();

        ResponseEntity.BodyBuilder response = ResponseEntity.status(downstream.getStatusCode());
        if (downstream.getHeaders().getContentType() != null) {
            response.contentType(downstream.getHeaders().getContentType());
        }
        return response.body(downstream.getBody());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal user) {
        return orderWebClient.delete()
                .uri("/api/orders/{id}", id)
                .header("User-Id", String.valueOf(user.getUserId()))
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    @GetMapping("/history")
    public ResponseEntity<List<OrderResponseDto>> getHistory(@AuthenticationPrincipal UserPrincipal user) {
        return orderWebClient.get()
                .uri("/api/orders/history")
                .header("User-Id", String.valueOf(user.getUserId()))
                .retrieve()
                .toEntityList(OrderResponseDto.class)
                .block();
    }
}
