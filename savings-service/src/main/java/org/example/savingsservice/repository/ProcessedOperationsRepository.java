package org.example.savingsservice.repository;

import org.example.savingsservice.entity.ProcessedOperation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProcessedOperationsRepository extends JpaRepository<ProcessedOperation, UUID> {
    @Modifying
    @Query(value = "INSERT INTO processed_operations(operation_id) VALUES (:operationId) ON CONFLICT (operation_id) DO NOTHING", nativeQuery = true)
    int insertIfAbsent(@Param("operationId") UUID operationId);
}
