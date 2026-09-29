package org.example.orderservice.repository;

import jakarta.persistence.LockModeType;
import org.example.orderservice.entity.QuoteVolumeConsumption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface QuoteVolumeConsumptionRepository extends JpaRepository<QuoteVolumeConsumption, String> {
    @Modifying
    @Query(value = "INSERT INTO quote_volume_consumption(snapshot_key, security_id, market_data_at, ask_used, bid_used) " +
            "VALUES (:snapshotKey, :securityId, :marketDataAt, 0, 0) ON CONFLICT (snapshot_key) DO NOTHING", nativeQuery = true)
    int createSnapshotIfAbsent(@Param("snapshotKey") String snapshotKey,
                               @Param("securityId") Long securityId,
                               @Param("marketDataAt") java.time.Instant marketDataAt);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select q from QuoteVolumeConsumption q where q.snapshotKey = :snapshotKey")
    Optional<QuoteVolumeConsumption> lockBySnapshotKey(@Param("snapshotKey") String snapshotKey);
}
