package org.example.orderservice.dto.request;

import java.math.BigDecimal;

public class SecurityDto {
    private Long id;
    private String ticker;
    private String companyName;
    private String isin;
    private String securityType;
    private String currencyCode;
    private String stockExchangeName;
    private BigDecimal lastPrice;
    private Integer lotSize;

    public SecurityDto() {}

    public SecurityDto(Long id, String ticker, String companyName, String isin, String securityType, String currencyCode, String stockExchangeName, BigDecimal lastPrice) {
        this.id = id;
        this.ticker = ticker;
        this.companyName = companyName;
        this.isin = isin;
        this.securityType = securityType;
        this.currencyCode = currencyCode;
        this.stockExchangeName = stockExchangeName;
        this.lastPrice = lastPrice;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getIsin() {
        return isin;
    }

    public void setIsin(String isin) {
        this.isin = isin;
    }

    public String getSecurityType() {
        return securityType;
    }

    public void setSecurityType(String securityType) {
        this.securityType = securityType;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getStockExchangeName() {
        return stockExchangeName;
    }

    public void setStockExchangeName(String stockExchangeName) {
        this.stockExchangeName = stockExchangeName;
    }

    public BigDecimal getLastPrice() {
        return lastPrice;
    }

    public void setLastPrice(BigDecimal lastPrice) {
        this.lastPrice = lastPrice;
    }

    public Integer getLotSize() { return lotSize; }
    public void setLotSize(Integer lotSize) { this.lotSize = lotSize; }
}
