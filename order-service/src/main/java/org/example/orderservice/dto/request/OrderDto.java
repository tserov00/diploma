package org.example.orderservice.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.example.orderservice.enumeration.OrderSideEnum;
import org.example.orderservice.enumeration.OrderTypeEnum;

import java.math.BigDecimal;

public class OrderDto {
    @NotNull
    private Long securityId;
    @NotNull
    private OrderSideEnum side;
    @NotNull
    private OrderTypeEnum orderType;
    @Positive
    private BigDecimal limitPrice;
    @NotNull
    @Positive
    private Integer quantity;

    public OrderDto() {}
    public Long getSecurityId() { return securityId; }
    public void setSecurityId(Long securityId) { this.securityId = securityId; }
    public OrderSideEnum getSide() { return side; }
    public void setSide(OrderSideEnum side) { this.side = side; }
    public OrderTypeEnum getOrderType() { return orderType; }
    public void setOrderType(OrderTypeEnum orderType) { this.orderType = orderType; }
    public BigDecimal getLimitPrice() { return limitPrice; }
    public void setLimitPrice(BigDecimal limitPrice) { this.limitPrice = limitPrice; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
