package org.example.orderservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "quote_volume_consumption")
public class QuoteVolumeConsumption {
    @Id
    @Column(name = "snapshot_key", length = 250)
    private String snapshotKey;

    @Column(name = "security_id", nullable = false)
    private Long securityId;

    @Column(name = "ask_used", nullable = false)
    private Long askUsed = 0L;

    @Column(name = "bid_used", nullable = false)
    private Long bidUsed = 0L;

    @Column(name = "market_data_at", nullable = false)
    private Instant marketDataAt;

    public QuoteVolumeConsumption() {}
    public QuoteVolumeConsumption(String snapshotKey, Long securityId, Instant marketDataAt) {
        this.snapshotKey = snapshotKey;
        this.securityId = securityId;
        this.marketDataAt = marketDataAt;
    }
    public String getSnapshotKey() { return snapshotKey; }
    public Long getSecurityId() { return securityId; }
    public Long getAskUsed() { return askUsed; }
    public void setAskUsed(Long askUsed) { this.askUsed = askUsed; }
    public Long getBidUsed() { return bidUsed; }
    public void setBidUsed(Long bidUsed) { this.bidUsed = bidUsed; }
    public Instant getMarketDataAt() { return marketDataAt; }
}
