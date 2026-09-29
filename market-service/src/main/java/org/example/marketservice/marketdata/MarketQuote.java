package org.example.marketservice.marketdata;

import java.math.BigDecimal;
import java.time.Instant;

public record MarketQuote(
        String ticker,
        BigDecimal lastPrice,
        BigDecimal bidPrice,
        BigDecimal askPrice,
        Long bidVolumeShares,
        Long askVolumeShares,
        Integer lotSize,
        BigDecimal dayHigh,
        BigDecimal dayLow,
        Long dayVolume,
        Instant lastPriceAt,
        Instant quoteUpdatedAt,
        Instant quoteReceivedAt,
        String source,
        String snapshotKey
) {}
