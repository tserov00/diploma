package org.example.orderservice.repository;

import jakarta.persistence.LockModeType;
import org.example.orderservice.entity.Order;
import org.example.orderservice.enumeration.OrderStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findAllByCustomerAccountIdOrderByCreatedAtDesc(Long customerAccountId);
    Optional<Order> findByIdAndCustomerAccountId(Long id, Long customerAccountId);
    List<Order> findAllByStatusOrderByCreatedAtAsc(OrderStatusEnum status);

    @Query("select distinct o.securityId from Order o where o.status in :statuses")
    List<Long> findActiveSecurityIds(@Param("statuses") List<OrderStatusEnum> statuses);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Order o where o.securityId = :securityId and o.status in :statuses order by o.createdAt asc")
    List<Order> lockActiveOrdersForSecurity(@Param("securityId") Long securityId,
                                            @Param("statuses") List<OrderStatusEnum> statuses);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Order o where o.id = :id")
    Optional<Order> lockById(@Param("id") Long id);
}
