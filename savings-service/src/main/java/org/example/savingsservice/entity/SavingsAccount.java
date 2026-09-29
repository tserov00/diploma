package org.example.savingsservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.Currency;


@Entity
@Table(name = "savings_accounts")
public class SavingsAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "savings_seq")
    @SequenceGenerator(name = "savings_seq", sequenceName = "savings_accounts_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "customer_account_id")
    private Long accountId;

    @Size(max = 30)
    @Column(name = "savings_account_number")
    private String savingsAccountNumber;

    @Column(name = "currency_id")
    private Long currencyId;

    @Column(name = "balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal balance;

    @Column(name = "reserved_amount", nullable = false, precision = 19, scale = 5)
    private BigDecimal reservedAmount;

    public SavingsAccount() {}

    public SavingsAccount(Long accountId, String savingsAccountNumber, Long currencyId, BigDecimal balance, BigDecimal reservedAmount) {
        this.accountId = accountId;
        this.savingsAccountNumber = savingsAccountNumber;
        this.currencyId = currencyId;
        this.balance = balance;
        this.reservedAmount = reservedAmount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getSavingsAccountNumber() {
        return savingsAccountNumber;
    }

    public void setSavingsAccountNumber(String savingsAccountNumber) {
        this.savingsAccountNumber = savingsAccountNumber;
    }

    public Long getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(Long currencyId) {
        this.currencyId = currencyId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getReservedAmount() {
        return reservedAmount;
    }

    public void setReservedAmount(BigDecimal reservedAmount) {
        this.reservedAmount = reservedAmount;
    }
}
