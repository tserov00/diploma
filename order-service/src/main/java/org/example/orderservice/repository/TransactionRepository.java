package org.example.orderservice.repository;

import jakarta.persistence.LockModeType;
import org.example.orderservice.entity.Transaction;
import org.example.orderservice.enumeration.TransactionStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findAllByOrder_CustomerAccountIdAndStatusOrderByCreatedAtDesc(
            Long customerAccountId, TransactionStatusEnum status);

    List<Transaction> findAllByStatusOrderByCreatedAtAsc(TransactionStatusEnum status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Transaction t where t.id = :id")
    Transaction lockById(@Param("id") Long id);
}
