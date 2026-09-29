package org.example.orderservice.dto.request;

import java.math.BigDecimal;
import java.time.Instant;

public class MarketQuoteDto {
    private Long securityId;
    private String ticker;
    private BigDecimal lastPrice;
    private BigDecimal bidPrice;
    private BigDecimal askPrice;
    private Long bidVolume;
    private Long askVolume;
    private Integer lotSize;
    private Instant lastPriceAt;
    private Instant quoteUpdatedAt;
    private Instant quoteReceivedAt;
    private String source;
    private boolean delayed;
    private String snapshotKey;

    public MarketQuoteDto() {}
    public Long getSecurityId() { return securityId; }
    public void setSecurityId(Long securityId) { this.securityId = securityId; }
    public String getTicker() { return ticker; }
    public void setTicker(String ticker) { this.ticker = ticker; }
    public BigDecimal getLastPrice() { return lastPrice; }
    public void setLastPrice(BigDecimal lastPrice) { this.lastPrice = lastPrice; }
    public BigDecimal getBidPrice() { return bidPrice; }
    public void setBidPrice(BigDecimal bidPrice) { this.bidPrice = bidPrice; }
    public BigDecimal getAskPrice() { return askPrice; }
    public void setAskPrice(BigDecimal askPrice) { this.askPrice = askPrice; }
    public Long getBidVolume() { return bidVolume; }
    public void setBidVolume(Long bidVolume) { this.bidVolume = bidVolume; }
    public Long getAskVolume() { return askVolume; }
    public void setAskVolume(Long askVolume) { this.askVolume = askVolume; }
    public Integer getLotSize() { return lotSize; }
    public void setLotSize(Integer lotSize) { this.lotSize = lotSize; }
    public Instant getLastPriceAt() { return lastPriceAt; }
    public void setLastPriceAt(Instant lastPriceAt) { this.lastPriceAt = lastPriceAt; }
    public Instant getQuoteUpdatedAt() { return quoteUpdatedAt; }
    public void setQuoteUpdatedAt(Instant quoteUpdatedAt) { this.quoteUpdatedAt = quoteUpdatedAt; }
    public Instant getQuoteReceivedAt() { return quoteReceivedAt; }
    public void setQuoteReceivedAt(Instant quoteReceivedAt) { this.quoteReceivedAt = quoteReceivedAt; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public boolean isDelayed() { return delayed; }
    public void setDelayed(boolean delayed) { this.delayed = delayed; }
    public String getSnapshotKey() { return snapshotKey; }
    public void setSnapshotKey(String snapshotKey) { this.snapshotKey = snapshotKey; }
}
