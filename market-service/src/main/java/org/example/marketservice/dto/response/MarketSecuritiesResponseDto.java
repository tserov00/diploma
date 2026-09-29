package org.example.marketservice.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public class MarketSecuritiesResponseDto {
    private Long id;
    private String ticker;
    private String companyName;
    private String isin;
    private String currencyCode;
    private String stockExchangeName;
    private Integer lotSize;
    private BigDecimal lastPrice;
    private BigDecimal bidPrice;
    private BigDecimal askPrice;
    private Long bidVolume;
    private Long askVolume;
    private BigDecimal dayHigh;
    private BigDecimal dayLow;
    private Long dayVolume;
    private Instant lastPriceAt;
    private Instant quoteUpdatedAt;
    private Instant quoteReceivedAt;
    private String quoteSource;

    public MarketSecuritiesResponseDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTicker() { return ticker; }
    public void setTicker(String ticker) { this.ticker = ticker; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getIsin() { return isin; }
    public void setIsin(String isin) { this.isin = isin; }
    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }
    public String getStockExchangeName() { return stockExchangeName; }
    public void setStockExchangeName(String stockExchangeName) { this.stockExchangeName = stockExchangeName; }
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
}
