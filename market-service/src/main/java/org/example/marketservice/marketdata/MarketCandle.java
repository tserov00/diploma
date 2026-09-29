package org.example.marketservice.marketdata;

import java.math.BigDecimal;
import java.time.Instant;

public record MarketCandle(Instant time, BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close, Long volume) {}
