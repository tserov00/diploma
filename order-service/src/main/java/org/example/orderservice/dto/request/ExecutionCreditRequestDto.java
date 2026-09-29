package org.example.orderservice.dto.request;

import java.math.BigDecimal;

public class ExecutionCreditRequestDto {
    private Long userId;
    private Long currencyId;
    private BigDecimal amount;
    public ExecutionCreditRequestDto() {}
    public ExecutionCreditRequestDto(Long userId, Long currencyId, BigDecimal amount) {
        this.userId = userId; this.currencyId = currencyId; this.amount = amount;
    }
    public Long getUserId() { return userId; }
    public Long getCurrencyId() { return currencyId; }
    public BigDecimal getAmount() { return amount; }
}
