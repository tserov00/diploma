package org.example.orderservice.repository;

import org.example.orderservice.entity.CustomerPortfolio;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerPortfolioRepository extends JpaRepository<CustomerPortfolio, CustomerPortfolio.CustomerPortfolioId> {
    List<CustomerPortfolio> findAllByCustomerAccountId(Long customerId);
    Optional<CustomerPortfolio> findByCustomerAccountIdAndSecurityId(Long customerAccountId, Long securityId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from CustomerPortfolio p where p.customerAccountId = :customerId and p.securityId = :securityId")
    Optional<CustomerPortfolio> lockByCustomerAndSecurity(@Param("customerId") Long customerId,
                                                           @Param("securityId") Long securityId);
}
