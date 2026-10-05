package org.example.orderservice.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.example.orderservice.enumeration.OrderSideEnum;
import org.example.orderservice.enumeration.OrderTypeEnum;

import java.math.BigDecimal;

public class OrderDto {
    @NotNull(message = "Идентификатор ценной бумаги обязателен")
    private Long securityId;
    @NotNull(message = "Сторона ордера обязательна")
    private OrderSideEnum side;
    @NotNull(message = "Тип ордера обязателен")
    private OrderTypeEnum orderType;
    @Positive(message = "Цена лимитного ордера должна быть больше нуля")
    private BigDecimal limitPrice;
    @NotNull(message = "Количество обязательно")
    @Positive(message = "Количество должно быть больше нуля")
    private Integer quantity;

    @AssertTrue(message = "Для лимитного ордера укажите цену; для рыночного ордера цена не указывается")
    public boolean isLimitPriceCompatibleWithOrderType() {
        if (orderType == null) return true;
        return orderType == OrderTypeEnum.LIMIT ? limitPrice != null : limitPrice == null;
    }

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
