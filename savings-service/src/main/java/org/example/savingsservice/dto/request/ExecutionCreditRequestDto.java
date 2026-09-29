package org.example.savingsservice.dto.request;

import java.math.BigDecimal;

public class ExecutionCreditRequestDto {
    private Long userId;
    private Long currencyId;
    private BigDecimal amount;
    public ExecutionCreditRequestDto() {}
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getCurrencyId() { return currencyId; }
    public void setCurrencyId(Long currencyId) { this.currencyId = currencyId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
