package org.example.orderservice.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public class TransactionResponseDto {
    private Long id;
    private String ticker;
    private String side;
    private String orderType;
    private BigDecimal executedPrice;
    private BigDecimal executedFee;
    private Integer quantity;
    private Long orderId;
    private Instant marketDataAt;
    private Instant executedAt;
    private String dataSource;
    public TransactionResponseDto() {}
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTicker() { return ticker; }
    public void setTicker(String ticker) { this.ticker = ticker; }
    public String getSide() { return side; }
    public void setSide(String side) { this.side = side; }
    public String getOrderType() { return orderType; }
    public void setOrderType(String orderType) { this.orderType = orderType; }
    public BigDecimal getExecutedPrice() { return executedPrice; }
    public void setExecutedPrice(BigDecimal executedPrice) { this.executedPrice = executedPrice; }
    public BigDecimal getExecutedFee() { return executedFee; }
    public void setExecutedFee(BigDecimal executedFee) { this.executedFee = executedFee; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Instant getMarketDataAt() { return marketDataAt; }
    public void setMarketDataAt(Instant marketDataAt) { this.marketDataAt = marketDataAt; }
    public Instant getExecutedAt() { return executedAt; }
    public void setExecutedAt(Instant executedAt) { this.executedAt = executedAt; }
    public String getDataSource() { return dataSource; }
    public void setDataSource(String dataSource) { this.dataSource = dataSource; }
}
