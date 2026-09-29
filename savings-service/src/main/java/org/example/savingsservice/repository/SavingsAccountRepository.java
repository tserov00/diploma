package org.example.savingsservice.repository;

import org.example.savingsservice.entity.SavingsAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavingsAccountRepository extends JpaRepository<SavingsAccount, Long> {
    Optional<SavingsAccount> findByCurrencyIdAndAccountId(Long currencyId, Long accountId);
    List<SavingsAccount> findAllByAccountId(Long customerAccountId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from SavingsAccount s where s.currencyId = :currencyId and s.accountId = :accountId")
    Optional<SavingsAccount> lockByCurrencyAndAccount(@Param("currencyId") Long currencyId, @Param("accountId") Long accountId);
}
