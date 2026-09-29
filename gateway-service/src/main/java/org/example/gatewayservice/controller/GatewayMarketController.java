package org.example.gatewayservice.controller;

import org.example.gatewayservice.dto.response.MarketSecuritiesResponseDto;
import org.example.gatewayservice.dto.response.MarketQuoteResponseDto;
import org.example.gatewayservice.dto.response.MarketCandleResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@RestController
@RequestMapping("/api/market")
public class GatewayMarketController {
    private final WebClient marketWebClient;

    public GatewayMarketController(WebClient marketWebClient) {
        this.marketWebClient = marketWebClient;
    }

    @GetMapping("/securities")
    public ResponseEntity<List<MarketSecuritiesResponseDto>> getSecurities() {
        return marketWebClient.get()
                .uri("/api/market/securities")
                .retrieve()
                .toEntityList(MarketSecuritiesResponseDto.class)
                .block();
    }

    @GetMapping("/securities/{id}")
    public ResponseEntity<MarketSecuritiesResponseDto> getSecurity(@org.springframework.web.bind.annotation.PathVariable Long id) {
        return marketWebClient.get()
                .uri("/api/market/securities/{id}", id)
                .retrieve()
                .toEntity(MarketSecuritiesResponseDto.class)
                .block();
    }

    @GetMapping("/securities/{id}/quote")
    public ResponseEntity<MarketQuoteResponseDto> getQuote(@org.springframework.web.bind.annotation.PathVariable Long id) {
        return marketWebClient.get()
                .uri("/api/market/securities/{id}/quote", id)
                .retrieve()
                .toEntity(MarketQuoteResponseDto.class)
                .block();
    }

    @GetMapping("/securities/{id}/history")
    public ResponseEntity<List<MarketCandleResponseDto>> getHistory(
            @org.springframework.web.bind.annotation.PathVariable Long id,
            @RequestParam(defaultValue = "1d") String timeframe,
            @RequestParam long from,
            @RequestParam long to) {
        return marketWebClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/market/securities/{id}/history")
                        .queryParam("timeframe", timeframe)
                        .queryParam("from", from)
                        .queryParam("to", to)
                        .build(id))
                .retrieve()
                .toEntityList(MarketCandleResponseDto.class)
                .block();
    }
}
