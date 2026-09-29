package org.example.marketservice.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public class MarketCandleResponseDto {
    private Instant time;
    private BigDecimal open;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal close;
    private Long volume;

    public MarketCandleResponseDto() {}
    public MarketCandleResponseDto(Instant time, BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close, Long volume) {
        this.time = time;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
    }
    public Instant getTime() { return time; }
    public void setTime(Instant time) { this.time = time; }
    public BigDecimal getOpen() { return open; }
    public void setOpen(BigDecimal open) { this.open = open; }
    public BigDecimal getHigh() { return high; }
    public void setHigh(BigDecimal high) { this.high = high; }
    public BigDecimal getLow() { return low; }
    public void setLow(BigDecimal low) { this.low = low; }
    public BigDecimal getClose() { return close; }
    public void setClose(BigDecimal close) { this.close = close; }
    public Long getVolume() { return volume; }
    public void setVolume(Long volume) { this.volume = volume; }
}
