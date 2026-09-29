package org.example.marketservice.controller;

import org.example.marketservice.dto.response.MarketCandleResponseDto;
import org.example.marketservice.dto.response.MarketQuoteResponseDto;
import org.example.marketservice.dto.response.MarketSecuritiesResponseDto;
import org.example.marketservice.service.MarketService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/market")
public class MarketController {
    private final MarketService marketService;

    public MarketController(MarketService marketService) { this.marketService = marketService; }

    @GetMapping("/securities")
    public ResponseEntity<List<MarketSecuritiesResponseDto>> getSecurities() {
        return ResponseEntity.ok(marketService.getSecurities());
    }

    @PostMapping("/securities/batch")
    public ResponseEntity<List<MarketSecuritiesResponseDto>> getSecuritiesBatchById(@RequestBody List<Long> ids) {
        return ResponseEntity.ok(marketService.getSecuritiesBatchByIds(ids));
    }

    @GetMapping("/securities/{id}")
    public ResponseEntity<MarketSecuritiesResponseDto> getSecurityById(@PathVariable Long id) {
        return ResponseEntity.ok(marketService.getSecurityById(id));
    }

    @GetMapping("/securities/{id}/quote")
    public ResponseEntity<MarketQuoteResponseDto> getQuote(@PathVariable Long id) {
        return ResponseEntity.ok(marketService.getQuoteById(id));
    }

    @GetMapping("/securities/{id}/history")
    public ResponseEntity<List<MarketCandleResponseDto>> getHistory(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1d") String timeframe,
            @RequestParam long from,
            @RequestParam long to) {
        return ResponseEntity.ok(marketService.getHistory(id, timeframe, from, to));
    }
}
