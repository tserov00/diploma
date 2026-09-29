package org.example.savingsservice.dto.request;

import java.math.BigDecimal;

public class ExecutionSettlementRequestDto {
    private Long userId;
    private Long currencyId;
    private BigDecimal reservedAmount;
    private BigDecimal debitAmount;
    public ExecutionSettlementRequestDto() {}
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getCurrencyId() { return currencyId; }
    public void setCurrencyId(Long currencyId) { this.currencyId = currencyId; }
    public BigDecimal getReservedAmount() { return reservedAmount; }
    public void setReservedAmount(BigDecimal reservedAmount) { this.reservedAmount = reservedAmount; }
    public BigDecimal getDebitAmount() { return debitAmount; }
    public void setDebitAmount(BigDecimal debitAmount) { this.debitAmount = debitAmount; }
}
