package org.example.orderservice.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "customer_portfolios")
@IdClass(CustomerPortfolio.CustomerPortfolioId.class)
public class CustomerPortfolio {

    @Id
    @Column(name = "customer_account_id")
    private Long customerAccountId;

    @Id
    @Column(name = "security_id")
    private Long securityId;

    @Column(name = "avg_buy_price", nullable = false, precision = 20, scale = 8)
    private BigDecimal avgBuyPrice = BigDecimal.ZERO;

    @Column(name = "total_quantity", nullable = false)
    private Integer totalQuantity = 0;

    @Column(name = "reserved_quantity", nullable = false)
    private Integer reservedQuantity = 0;

    @Column(name = "avg_sell_price", nullable = false, precision = 20, scale = 8)
    private BigDecimal avgSellPrice = BigDecimal.ZERO;

    @Column(name = "sold_quantity", nullable = false)
    private Integer soldQuantity = 0;

    public static class CustomerPortfolioId implements Serializable {
        private Long customerAccountId;
        private Long securityId;

        public CustomerPortfolioId() {}

        public CustomerPortfolioId(Long customerAccountId, Long securityId) {
            this.customerAccountId = customerAccountId;
            this.securityId = securityId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CustomerPortfolioId that = (CustomerPortfolioId) o;
            return Objects.equals(customerAccountId, that.customerAccountId) && Objects.equals(securityId, that.securityId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(customerAccountId, securityId);
        }
    }

    public CustomerPortfolio() {}

    public CustomerPortfolio(Long customerAccountId, Long securityId, BigDecimal avgBuyPrice, Integer totalQuantity, Integer reservedQuantity, BigDecimal avgSellPrice, Integer soldQuantity) {
        this.customerAccountId = customerAccountId;
        this.securityId = securityId;
        this.avgBuyPrice = avgBuyPrice;
        this.totalQuantity = totalQuantity;
        this.reservedQuantity = reservedQuantity;
        this.avgSellPrice = avgSellPrice;
        this.soldQuantity = soldQuantity;
    }

    public Long getCustomerAccountId() {
        return customerAccountId;
    }

    public void setCustomerAccountId(Long customerAccountId) {
        this.customerAccountId = customerAccountId;
    }

    public Long getSecurityId() {
        return securityId;
    }

    public void setSecurityId(Long securityId) {
        this.securityId = securityId;
    }

    public BigDecimal getAvgBuyPrice() {
        return avgBuyPrice;
    }

    public void setAvgBuyPrice(BigDecimal avgBuyPrice) {
        this.avgBuyPrice = avgBuyPrice;
    }

    public Integer getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(Integer totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public Integer getReservedQuantity() {
        return reservedQuantity;
    }

    public void setReservedQuantity(Integer reservedQuantity) {
        this.reservedQuantity = reservedQuantity;
    }

    public BigDecimal getAvgSellPrice() {
        return avgSellPrice;
    }

    public void setAvgSellPrice(BigDecimal avgSellPrice) {
        this.avgSellPrice = avgSellPrice;
    }

    public Integer getSoldQuantity() {
        return soldQuantity;
    }

    public void setSoldQuantity(Integer soldQuantity) {
        this.soldQuantity = soldQuantity;
    }
}
