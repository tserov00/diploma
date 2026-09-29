package org.example.orderservice.service;

import org.example.orderservice.client.SecurityClient;
import org.example.orderservice.dto.request.MarketQuoteDto;
import org.example.orderservice.entity.Transaction;
import org.example.orderservice.enumeration.OrderSideEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class OrderProcessor {
    private static final Logger log = LoggerFactory.getLogger(OrderProcessor.class);
    private final SecurityClient securityClient;
    private final OrderExecutionService executionService;
    private final FillSettlementService settlementService;
    private final long maxQuoteAgeSeconds;
    private final AtomicBoolean processing = new AtomicBoolean(false);

    public OrderProcessor(SecurityClient securityClient,
                          OrderExecutionService executionService,
                          FillSettlementService settlementService,
                          @Value("${market.quote.max-age-seconds:2700}") long maxQuoteAgeSeconds) {
        this.securityClient = securityClient;
        this.executionService = executionService;
        this.settlementService = settlementService;
        this.maxQuoteAgeSeconds = maxQuoteAgeSeconds;
    }

    @Scheduled(fixedDelayString = "${order.processing.interval-ms:10000}")
    public void processActiveOrders() {
        if (!processing.compareAndSet(false, true)) return;
        try {
            settlementService.recoverPendingOperations();
            for (Long securityId : executionService.activeSecurityIds()) {
                try {
                    MarketQuoteDto quote = securityClient.getQuoteById(securityId);
                    processQuote(securityId, quote);
                } catch (Exception e) {
                    log.warn("Не удалось обработать открытые заявки по инструменту {}: {}", securityId, e.getMessage());
                }
            }
        } finally {
            processing.set(false);
        }
    }

    public synchronized void processQuote(Long securityId, MarketQuoteDto quote) {
        if (!isUsable(quote)) return;
        for (OrderSideEnum side : OrderSideEnum.values()) {
            for (int fills = 0; fills < 1000; fills++) {
                Transaction pending = executionService.prepareNextFill(securityId, quote, side);
                if (pending == null) break;
                try {
                    settlementService.settleFill(pending.getId());
                } catch (Exception e) {
                    log.warn("Исполнение {} сохранено для повторной обработки: {}", pending.getId(), e.getMessage());
                }
            }
        }
    }

    public boolean isUsable(MarketQuoteDto quote) {
        if (quote == null || quote.getSnapshotKey() == null || quote.getQuoteUpdatedAt() == null) return false;
        if (quote.getQuoteUpdatedAt().isAfter(Instant.now().plusSeconds(60))) return false;
        long age = Duration.between(quote.getQuoteUpdatedAt(), Instant.now()).getSeconds();
        return age <= maxQuoteAgeSeconds;
    }
}
