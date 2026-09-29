package org.example.orderservice.entity;

import jakarta.persistence.*;
import org.example.orderservice.enumeration.OrderSideEnum;
import org.example.orderservice.enumeration.OrderStatusEnum;
import org.example.orderservice.enumeration.OrderTypeEnum;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "orders", indexes = {
        @Index(name = "orders_active_security_idx", columnList = "security_id,status,created_at"),
        @Index(name = "orders_customer_created_idx", columnList = "customer_account_id,created_at")
})
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_account_id", nullable = false)
    private Long customerAccountId;

    @Column(name = "security_id", nullable = false)
    private Long securityId;

    @Column(name = "savings_account_id", nullable = false)
    private Long savingsAccountId;

    @Column(name = "currency_id", nullable = false)
    private Long currencyId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private OrderSideEnum side;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false, length = 8)
    private OrderTypeEnum orderType;

    @Column(name = "limit_price", precision = 20, scale = 6)
    private BigDecimal limitPrice;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "remaining_quantity", nullable = false)
    private Integer remainingQuantity;

    @Column(name = "reserve_per_unit", precision = 20, scale = 6)
    private BigDecimal reservePerUnit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatusEnum status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Order() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCustomerAccountId() { return customerAccountId; }
    public void setCustomerAccountId(Long customerAccountId) { this.customerAccountId = customerAccountId; }
    public Long getSecurityId() { return securityId; }
    public void setSecurityId(Long securityId) { this.securityId = securityId; }
    public Long getSavingsAccountId() { return savingsAccountId; }
    public void setSavingsAccountId(Long savingsAccountId) { this.savingsAccountId = savingsAccountId; }
    public Long getCurrencyId() { return currencyId; }
    public void setCurrencyId(Long currencyId) { this.currencyId = currencyId; }
    public OrderSideEnum getSide() { return side; }
    public void setSide(OrderSideEnum side) { this.side = side; }
    public OrderTypeEnum getOrderType() { return orderType; }
    public void setOrderType(OrderTypeEnum orderType) { this.orderType = orderType; }
    public BigDecimal getLimitPrice() { return limitPrice; }
    public void setLimitPrice(BigDecimal limitPrice) { this.limitPrice = limitPrice; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Integer getRemainingQuantity() { return remainingQuantity; }
    public void setRemainingQuantity(Integer remainingQuantity) { this.remainingQuantity = remainingQuantity; }
    public BigDecimal getReservePerUnit() { return reservePerUnit; }
    public void setReservePerUnit(BigDecimal reservePerUnit) { this.reservePerUnit = reservePerUnit; }
    public OrderStatusEnum getStatus() { return status; }
    public void setStatus(OrderStatusEnum status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
