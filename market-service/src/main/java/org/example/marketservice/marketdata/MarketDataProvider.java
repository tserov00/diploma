package org.example.marketservice.marketdata;

import java.time.Instant;
import java.util.List;

public interface MarketDataProvider {
    MarketQuote getQuote(String exchange, String ticker);
    List<MarketCandle> getHistory(String exchange, String ticker, String timeframe, Instant from, Instant to);
    String sourceName();
    boolean isDelayed();
}
