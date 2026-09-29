package org.example.orderservice.service;

import org.example.orderservice.client.SavingsAccountClient;
import org.example.orderservice.dto.request.ExecutionCreditRequestDto;
import org.example.orderservice.dto.request.ExecutionSettlementRequestDto;
import org.example.orderservice.entity.Order;
import org.example.orderservice.entity.Transaction;
import org.example.orderservice.enumeration.OrderSideEnum;
import org.example.orderservice.enumeration.OrderStatusEnum;
import org.example.orderservice.enumeration.TransactionStatusEnum;
import org.example.orderservice.repository.OrderRepository;
import org.example.orderservice.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
public class FillSettlementService {
    private static final Logger log = LoggerFactory.getLogger(FillSettlementService.class);

    private final TransactionRepository transactionRepository;
    private final OrderRepository orderRepository;
    private final SavingsAccountClient savingsAccountClient;
    private final CustomerPortfolioService portfolioService;
    private final OrderExecutionService orderExecutionService;

    public FillSettlementService(TransactionRepository transactionRepository,
                                 OrderRepository orderRepository,
                                 SavingsAccountClient savingsAccountClient,
                                 CustomerPortfolioService portfolioService,
                                 OrderExecutionService orderExecutionService) {
        this.transactionRepository = transactionRepository;
        this.orderRepository = orderRepository;
        this.savingsAccountClient = savingsAccountClient;
        this.portfolioService = portfolioService;
        this.orderExecutionService = orderExecutionService;
    }

    public void settleFill(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId).orElse(null);
        if (transaction == null || transaction.getStatus() == TransactionStatusEnum.EXECUTED) return;
        Order order = transaction.getOrder();
        int quantity = transaction.getQuantity();
        BigDecimal feePerUnit = FeeCalculator.calculateFeePerUnit(transaction.getExecutedPrice());
        UUID reference = transaction.getExecutionRef();

        if (transaction.getSide() == OrderSideEnum.BUY) {
            BigDecimal reserveRelease = order.getReservePerUnit().multiply(BigDecimal.valueOf(quantity));
            BigDecimal debit = transaction.getExecutedPrice().multiply(BigDecimal.valueOf(quantity))
                    .add(transaction.getExecutedFee());
            savingsAccountClient.settleReservedExecution(
                    new ExecutionSettlementRequestDto(order.getCustomerAccountId(), order.getCurrencyId(), reserveRelease, debit),
                    operationId(reference, "cash"));
            portfolioService.applyBuyExecution(order.getCustomerAccountId(), order.getSecurityId(), quantity,
                    transaction.getExecutedPrice().add(feePerUnit), operationId(reference, "portfolio"));
        } else {
            portfolioService.applySellExecution(order.getCustomerAccountId(), order.getSecurityId(), quantity,
                    transaction.getExecutedPrice().subtract(feePerUnit), operationId(reference, "portfolio"));
            BigDecimal proceeds = transaction.getExecutedPrice().multiply(BigDecimal.valueOf(quantity))
                    .subtract(transaction.getExecutedFee());
            savingsAccountClient.creditExecution(
                    new ExecutionCreditRequestDto(order.getCustomerAccountId(), order.getCurrencyId(), proceeds),
                    operationId(reference, "cash"));
        }

        OrderStatusEnum status = orderExecutionService.completeFill(transactionId);
        if (status == OrderStatusEnum.CANCEL_PENDING) settleCancellation(order.getId());
    }

    public void cancelLimitOrder(Long orderId, Long userId) {
        Order order = orderExecutionService.beginCancellation(orderId, userId);
        if (order != null) settleCancellation(orderId);
    }

    public void cancelUnfilledMarketOrder(Long orderId) {
        Order order = orderExecutionService.beginUnfilledMarketCancellation(orderId);
        if (order != null) settleCancellation(orderId);
    }

    public void recoverPendingOperations() {
        for (Long transactionId : orderExecutionService.pendingTransactionIds()) {
            try { settleFill(transactionId); }
            catch (Exception e) { log.warn("Оставляю исполнение {} на повтор: {}", transactionId, e.getMessage()); }
        }
        for (Long orderId : orderExecutionService.pendingCancellationIds()) {
            try { settleCancellation(orderId); }
            catch (Exception e) { log.warn("Оставляю отмену заявки {} на повтор: {}", orderId, e.getMessage()); }
        }
    }

    private void settleCancellation(Long orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null || order.getStatus() != OrderStatusEnum.CANCEL_PENDING) return;
        int remaining = order.getRemainingQuantity() == null ? 0 : order.getRemainingQuantity();
        if (remaining > 0 && order.getSide() == OrderSideEnum.BUY) {
            BigDecimal release = order.getReservePerUnit().multiply(BigDecimal.valueOf(remaining));
            savingsAccountClient.releaseAmount(
                    new org.example.orderservice.dto.request.ReserveAmountRequestDto(order.getCustomerAccountId(), order.getCurrencyId(), release),
                    orderOperationId(orderId, "cancel-cash"));
        } else if (remaining > 0) {
            portfolioService.releaseReservedShares(order.getCustomerAccountId(), order.getSecurityId(), remaining,
                    orderOperationId(orderId, "cancel-shares"));
        }
        orderExecutionService.finishCancellation(orderId);
    }

    private UUID operationId(UUID reference, String suffix) {
        return UUID.nameUUIDFromBytes((reference + ":" + suffix).getBytes(StandardCharsets.UTF_8));
    }

    private UUID orderOperationId(Long orderId, String suffix) {
        return UUID.nameUUIDFromBytes(("order:" + orderId + ":" + suffix).getBytes(StandardCharsets.UTF_8));
    }
}
