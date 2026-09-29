package org.example.marketservice.marketdata;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "market.data.provider", havingValue = "DEMO")
public class DemoMarketDataProvider implements MarketDataProvider {
    private static final Map<String, BigDecimal> BASE_PRICES = Map.ofEntries(
            Map.entry("SBER", new BigDecimal("300.00")),
            Map.entry("SBERP", new BigDecimal("290.00")),
            Map.entry("GAZP", new BigDecimal("170.00")),
            Map.entry("LKOH", new BigDecimal("7000.00")),
            Map.entry("YDEX", new BigDecimal("4000.00")),
            Map.entry("ROSN", new BigDecimal("500.00")),
            Map.entry("GMKN", new BigDecimal("150.00")),
            Map.entry("NVTK", new BigDecimal("1000.00")),
            Map.entry("TATN", new BigDecimal("700.00")),
            Map.entry("VTBR", new BigDecimal("0.03")),
            Map.entry("MOEX", new BigDecimal("200.00")),
            Map.entry("NLMK", new BigDecimal("150.00")),
            Map.entry("CHMF", new BigDecimal("1100.00")),
            Map.entry("ALRS", new BigDecimal("70.00")),
            Map.entry("AFLT", new BigDecimal("60.00")),
            Map.entry("PLZL", new BigDecimal("15000.00")),
            Map.entry("MTSS", new BigDecimal("250.00")),
            Map.entry("MGNT", new BigDecimal("5000.00")),
            Map.entry("SNGS", new BigDecimal("25.00")),
            Map.entry("SNGSP", new BigDecimal("50.00")),
            Map.entry("PHOR", new BigDecimal("6500.00")),
            Map.entry("TRNFP", new BigDecimal("140000.00")),
            Map.entry("BSPB", new BigDecimal("400.00")),
            Map.entry("FIVE", new BigDecimal("2500.00")),
            Map.entry("MAGN", new BigDecimal("50.00")),
            Map.entry("RUAL", new BigDecimal("35.00")),
            Map.entry("FESH", new BigDecimal("70.00")),
            Map.entry("IRAO", new BigDecimal("4.00")),
            Map.entry("PIKK", new BigDecimal("600.00")),
            Map.entry("NMTP", new BigDecimal("10.00"))
    );

    @Override
    public MarketQuote getQuote(String exchange, String ticker) {
        BigDecimal base = BASE_PRICES.getOrDefault(ticker, new BigDecimal("100.00"));
        Instant quoteAt = Instant.now().truncatedTo(ChronoUnit.MINUTES);
        long minute = quoteAt.getEpochSecond() / 60;
        double variation = Math.sin((minute % 240) / 18.0 + (ticker.hashCode() & 255)) * 0.006;
        BigDecimal last = base.multiply(BigDecimal.valueOf(1 + variation)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal spread = base.compareTo(BigDecimal.ONE) < 0 ? new BigDecimal("0.0001") : new BigDecimal("0.01");
        BigDecimal bid = last.subtract(spread).max(new BigDecimal("0.0001"));
        BigDecimal ask = last.add(spread);
        int lotSize = lotSize(ticker);
        long lots = 500;
        return new MarketQuote(ticker, last, bid, ask, lots * lotSize, lots * lotSize, lotSize,
                last.add(spread.multiply(BigDecimal.TEN)), last.subtract(spread.multiply(BigDecimal.TEN)),
                100_000L, quoteAt, quoteAt, Instant.now(), sourceName(),
                String.join("|", exchange, ticker, quoteAt.toString(), bid.toPlainString(), ask.toPlainString(), Long.toString(lots)));
    }

    @Override
    public List<MarketCandle> getHistory(String exchange, String ticker, String timeframe, Instant from, Instant to) {
        BigDecimal base = BASE_PRICES.getOrDefault(ticker, new BigDecimal("100.00"));
        long step = switch (timeframe == null ? "1d" : timeframe) {
            case "1m" -> 60;
            case "5m" -> 300;
            case "1h" -> 3600;
            case "1w" -> 604800;
            case "1M" -> 2_592_000;
            default -> 86400;
        };
        long first = Math.max(from.getEpochSecond(), to.getEpochSecond() - step * 250L);
        first = first - Math.floorMod(first, step);
        List<MarketCandle> candles = new ArrayList<>();
        BigDecimal previous = base;
        for (long time = first; time <= to.getEpochSecond() && candles.size() < 300; time += step) {
            double wave = Math.sin(time / (double) step / 7.0 + ticker.hashCode()) * 0.008;
            BigDecimal close = base.multiply(BigDecimal.valueOf(1 + wave)).setScale(2, RoundingMode.HALF_UP);
            BigDecimal high = close.max(previous).multiply(new BigDecimal("1.002")).setScale(2, RoundingMode.HALF_UP);
            BigDecimal low = close.min(previous).multiply(new BigDecimal("0.998")).setScale(2, RoundingMode.HALF_UP);
            candles.add(new MarketCandle(Instant.ofEpochSecond(time), previous, high, low, close, 1_000L));
            previous = close;
        }
        return candles;
    }

    @Override
    public String sourceName() { return "DEMO"; }

    @Override
    public boolean isDelayed() { return false; }

    private int lotSize(String ticker) {
        return switch (ticker) {
            case "SBER", "SBERP", "GAZP", "NLMK", "ALRS", "AFLT", "MTSS" -> 10;
            case "SNGS", "SNGSP" -> 100;
            case "VTBR" -> 10_000;
            default -> 1;
        };
    }
}
