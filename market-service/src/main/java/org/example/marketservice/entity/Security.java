package org.example.marketservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "securities")
public class Security {
    @Id
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String ticker;

    @Column(length = 12)
    private String isin;

    @Column(name = "company_name", nullable = false, length = 120)
    private String companyName;

    @Column(name = "exchange_code", nullable = false, length = 10)
    private String exchangeCode;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @Column(name = "lot_size", nullable = false)
    private Integer lotSize = 1;

    @Column(name = "last_price", precision = 20, scale = 6)
    private BigDecimal lastPrice;

    @Column(name = "bid_price", precision = 20, scale = 6)
    private BigDecimal bidPrice;

    @Column(name = "ask_price", precision = 20, scale = 6)
    private BigDecimal askPrice;

    // ALOR reports best-level volumes in lots; these cached values are normalized to shares.
    @Column(name = "bid_volume")
    private Long bidVolume;

    @Column(name = "ask_volume")
    private Long askVolume;

    @Column(name = "day_high", precision = 20, scale = 6)
    private BigDecimal dayHigh;

    @Column(name = "day_low", precision = 20, scale = 6)
    private BigDecimal dayLow;

    @Column(name = "day_volume")
    private Long dayVolume;

    @Column(name = "last_price_at")
    private Instant lastPriceAt;

    @Column(name = "quote_updated_at")
    private Instant quoteUpdatedAt;

    @Column(name = "quote_received_at")
    private Instant quoteReceivedAt;

    @Column(name = "quote_source", length = 30)
    private String quoteSource;

    @Column(name = "snapshot_key", length = 250)
    private String snapshotKey;

    public Security() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTicker() { return ticker; }
    public void setTicker(String ticker) { this.ticker = ticker; }
    public String getIsin() { return isin; }
    public void setIsin(String isin) { this.isin = isin; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getExchangeCode() { return exchangeCode; }
    public void setExchangeCode(String exchangeCode) { this.exchangeCode = exchangeCode; }
    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }
    public Integer getLotSize() { return lotSize; }
    public void setLotSize(Integer lotSize) { this.lotSize = lotSize; }
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
    public BigDecimal getDayHigh() { return dayHigh; }
    public void setDayHigh(BigDecimal dayHigh) { this.dayHigh = dayHigh; }
    public BigDecimal getDayLow() { return dayLow; }
    public void setDayLow(BigDecimal dayLow) { this.dayLow = dayLow; }
    public Long getDayVolume() { return dayVolume; }
    public void setDayVolume(Long dayVolume) { this.dayVolume = dayVolume; }
    public Instant getLastPriceAt() { return lastPriceAt; }
    public void setLastPriceAt(Instant lastPriceAt) { this.lastPriceAt = lastPriceAt; }
    public Instant getQuoteUpdatedAt() { return quoteUpdatedAt; }
    public void setQuoteUpdatedAt(Instant quoteUpdatedAt) { this.quoteUpdatedAt = quoteUpdatedAt; }
    public Instant getQuoteReceivedAt() { return quoteReceivedAt; }
    public void setQuoteReceivedAt(Instant quoteReceivedAt) { this.quoteReceivedAt = quoteReceivedAt; }
    public String getQuoteSource() { return quoteSource; }
    public void setQuoteSource(String quoteSource) { this.quoteSource = quoteSource; }
    public String getSnapshotKey() { return snapshotKey; }
    public void setSnapshotKey(String snapshotKey) { this.snapshotKey = snapshotKey; }
}
