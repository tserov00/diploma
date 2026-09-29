package org.example.orderservice.entity;

import jakarta.persistence.*;
import org.example.orderservice.enumeration.OrderSideEnum;
import org.example.orderservice.enumeration.OrderTypeEnum;
import org.example.orderservice.enumeration.TransactionStatusEnum;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transactions", indexes = {
        @Index(name = "transactions_order_idx", columnList = "order_id,created_at")
})
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "execution_ref", nullable = false, unique = true)
    private UUID executionRef;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 4)
    private OrderSideEnum side;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false, length = 8)
    private OrderTypeEnum orderType;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "executed_price", nullable = false, precision = 20, scale = 6)
    private BigDecimal executedPrice;

    @Column(name = "executed_fee", nullable = false, precision = 20, scale = 6)
    private BigDecimal executedFee;

    @Column(name = "market_data_at", nullable = false)
    private Instant marketDataAt;

    @Column(name = "data_source", nullable = false, length = 30)
    private String dataSource;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private TransactionStatusEnum status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "executed_at")
    private Instant executedAt;

    public Transaction() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public UUID getExecutionRef() { return executionRef; }
    public void setExecutionRef(UUID executionRef) { this.executionRef = executionRef; }
    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
    public OrderSideEnum getSide() { return side; }
    public void setSide(OrderSideEnum side) { this.side = side; }
    public OrderTypeEnum getOrderType() { return orderType; }
    public void setOrderType(OrderTypeEnum orderType) { this.orderType = orderType; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getExecutedPrice() { return executedPrice; }
    public void setExecutedPrice(BigDecimal executedPrice) { this.executedPrice = executedPrice; }
    public BigDecimal getExecutedFee() { return executedFee; }
    public void setExecutedFee(BigDecimal executedFee) { this.executedFee = executedFee; }
    public Instant getMarketDataAt() { return marketDataAt; }
    public void setMarketDataAt(Instant marketDataAt) { this.marketDataAt = marketDataAt; }
    public String getDataSource() { return dataSource; }
    public void setDataSource(String dataSource) { this.dataSource = dataSource; }
    public TransactionStatusEnum getStatus() { return status; }
    public void setStatus(TransactionStatusEnum status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getExecutedAt() { return executedAt; }
    public void setExecutedAt(Instant executedAt) { this.executedAt = executedAt; }
}
