package org.example.marketservice.marketdata;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
@ConditionalOnProperty(name = "market.data.provider", havingValue = "ALOR", matchIfMissing = true)
public class AlorMarketDataProvider implements MarketDataProvider {
    private final RestTemplate restTemplate;
    private final String baseUrl;

    public AlorMarketDataProvider(RestTemplate restTemplate,
                                  @Value("${market.data.alor.base-url:https://api.alor.ru}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl.replaceAll("/$", "");
    }

    @Override
    public MarketQuote getQuote(String exchange, String ticker) {
        String url = baseUrl + "/md/v2/Securities/" + exchange + ":" + ticker + "/quotes?format=Simple";
        JsonNode root = restTemplate.getForObject(url, JsonNode.class);
        JsonNode quote = unwrap(root);

        int lotSize = Math.max(1, integer(quote, 1, "lotsize", "lotSize", "lot"));
        BigDecimal last = decimal(quote, "last_price", "lastPrice", "c");
        BigDecimal bid = decimal(quote, "bid");
        BigDecimal ask = decimal(quote, "ask");
        Long bidLots = integerLong(quote, "bid_vol", "bidVol", "bv");
        Long askLots = integerLong(quote, "ask_vol", "askVol", "av");
        Instant lastAt = instant(quote, "last_price_timestamp", "lastPriceTimestamp", "tst", "timestamp");
        Instant bookAt = instant(quote, "ob_ms_timestamp", "orderbook_timestamp", "book_timestamp", "tso");
        if (bookAt == null) {
            bookAt = instant(quote, "timestamp");
        }
        Instant receivedAt = Instant.now();

        String snapshotKey = String.join("|",
                exchange,
                ticker,
                bookAt != null ? bookAt.toString() : "no-book-time",
                text(bid), text(ask), text(bidLots), text(askLots), Integer.toString(lotSize));

        return new MarketQuote(
                ticker,
                last,
                bid,
                ask,
                bidLots == null ? null : bidLots * lotSize,
                askLots == null ? null : askLots * lotSize,
                lotSize,
                decimal(quote, "high_price", "highPrice", "h"),
                decimal(quote, "low_price", "lowPrice", "l"),
                integerLong(quote, "volume", "v"),
                lastAt,
                bookAt,
                receivedAt,
                sourceName(),
                snapshotKey
        );
    }

    @Override
    public List<MarketCandle> getHistory(String exchange, String ticker, String timeframe, Instant from, Instant to) {
        String url = UriComponentsBuilder.fromUriString(baseUrl + "/md/v2/history")
                .queryParam("symbol", ticker)
                .queryParam("exchange", exchange)
                .queryParam("tf", mapTimeframe(timeframe))
                .queryParam("from", from.getEpochSecond())
                .queryParam("to", to.getEpochSecond())
                .queryParam("instrumentGroup", "TQBR")
                .queryParam("format", "Simple")
                .build()
                .encode()
                .toUriString();

        JsonNode root = restTemplate.getForObject(url, JsonNode.class);
        JsonNode rows = historyRows(root);
        List<MarketCandle> candles = new ArrayList<>();
        if (rows == null || !rows.isArray()) {
            return candles;
        }

        for (JsonNode row : rows) {
            Instant time = instant(row, "time", "t");
            BigDecimal open = decimal(row, "open", "o");
            BigDecimal high = decimal(row, "high", "h");
            BigDecimal low = decimal(row, "low", "l");
            BigDecimal close = decimal(row, "close", "c");
            if (time != null && open != null && high != null && low != null && close != null) {
                candles.add(new MarketCandle(time, open, high, low, close, integerLong(row, "volume", "v")));
            }
        }
        return candles;
    }

    @Override
    public String sourceName() { return "ALOR"; }

    @Override
    public boolean isDelayed() { return true; }

    private JsonNode unwrap(JsonNode root) {
        if (root == null || root.isNull()) {
            throw new IllegalStateException("ALOR вернул пустой ответ котировки");
        }
        if (root.has("data") && root.get("data").isObject()) return root.get("data");
        if (root.has("quote") && root.get("quote").isObject()) return root.get("quote");
        return root;
    }

    private JsonNode historyRows(JsonNode root) {
        if (root == null || root.isNull()) return null;
        if (root.isArray()) return root;
        for (String field : List.of("history", "data", "candles")) {
            JsonNode child = root.get(field);
            if (child != null && child.isArray()) return child;
        }
        return null;
    }

    private String mapTimeframe(String timeframe) {
        return switch (timeframe == null ? "1d" : timeframe) {
            case "1m" -> "60";
            case "5m" -> "300";
            case "1h" -> "3600";
            case "1w" -> "W";
            case "1M" -> "M";
            default -> "D";
        };
    }

    private BigDecimal decimal(JsonNode node, String... names) {
        JsonNode value = field(node, names);
        if (value == null || value.isNull() || value.asText().isBlank()) return null;
        try { return new BigDecimal(value.asText()); }
        catch (NumberFormatException ignored) { return null; }
    }

    private Integer integer(JsonNode node, int fallback, String... names) {
        Long value = integerLong(node, names);
        return value == null ? fallback : Math.toIntExact(value);
    }

    private Long integerLong(JsonNode node, String... names) {
        JsonNode value = field(node, names);
        if (value == null || value.isNull() || value.asText().isBlank()) return null;
        try { return value.isNumber() ? value.longValue() : Long.parseLong(value.asText()); }
        catch (NumberFormatException ignored) { return null; }
    }

    private Instant instant(JsonNode node, String... names) {
        Long value = integerLong(node, names);
        if (value == null || value <= 0) return null;
        return value > 10_000_000_000L ? Instant.ofEpochMilli(value) : Instant.ofEpochSecond(value);
    }

    private JsonNode field(JsonNode node, String... names) {
        if (node == null) return null;
        for (String name : names) {
            JsonNode value = node.get(name);
            if (value != null && !value.isMissingNode()) return value;
        }
        return null;
    }

    private String text(Object value) { return value == null ? "" : value.toString(); }
}
