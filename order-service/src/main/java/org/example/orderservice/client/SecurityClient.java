package org.example.orderservice.client;

import org.example.orderservice.dto.request.SecurityDto;
import org.example.orderservice.dto.request.MarketQuoteDto;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;

@Service
public class SecurityClient {
    private final WebClient marketWebClient;

    public SecurityClient(WebClient marketWebClient) {
        this.marketWebClient = marketWebClient;
    }

    public List<SecurityDto> getByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }

        return marketWebClient.post()
                .uri("/api/market/securities/batch")
                .bodyValue(ids)
                .retrieve()
                .bodyToFlux(SecurityDto.class)
                .collectList()
                .block();
    }

    public SecurityDto getById(Long id) {
        return marketWebClient.get()
                .uri("/api/market/securities/{id}", id)
                .retrieve()
                .bodyToMono(SecurityDto.class)
                .block();
    }

    public MarketQuoteDto getQuoteById(Long id) {
        return marketWebClient.get()
                .uri("/api/market/securities/{id}/quote", id)
                .retrieve()
                .bodyToMono(MarketQuoteDto.class)
                .block();
    }
}
