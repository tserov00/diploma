package org.example.orderservice.dto.request;

import java.math.BigDecimal;

public class ExecutionSettlementRequestDto {
    private Long userId;
    private Long currencyId;
    private BigDecimal reservedAmount;
    private BigDecimal debitAmount;
    public ExecutionSettlementRequestDto() {}
    public ExecutionSettlementRequestDto(Long userId, Long currencyId, BigDecimal reservedAmount, BigDecimal debitAmount) {
        this.userId = userId; this.currencyId = currencyId; this.reservedAmount = reservedAmount; this.debitAmount = debitAmount;
    }
    public Long getUserId() { return userId; }
    public Long getCurrencyId() { return currencyId; }
    public BigDecimal getReservedAmount() { return reservedAmount; }
    public BigDecimal getDebitAmount() { return debitAmount; }
}
