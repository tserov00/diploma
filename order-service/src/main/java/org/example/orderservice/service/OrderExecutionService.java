package org.example.orderservice.service;

import jakarta.transaction.Transactional;
import org.example.orderservice.dto.request.MarketQuoteDto;
import org.example.orderservice.entity.Order;
import org.example.orderservice.entity.QuoteVolumeConsumption;
import org.example.orderservice.entity.Transaction;
import org.example.orderservice.enumeration.OrderSideEnum;
import org.example.orderservice.enumeration.OrderStatusEnum;
import org.example.orderservice.enumeration.OrderTypeEnum;
import org.example.orderservice.enumeration.TransactionStatusEnum;
import org.example.orderservice.repository.OrderRepository;
import org.example.orderservice.repository.QuoteVolumeConsumptionRepository;
import org.example.orderservice.repository.TransactionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderExecutionService {
    private static final List<OrderStatusEnum> ACTIVE = List.of(OrderStatusEnum.OPEN, OrderStatusEnum.PARTIALLY_FILLED);

    private final OrderRepository orderRepository;
    private final TransactionRepository transactionRepository;
    private final QuoteVolumeConsumptionRepository volumeRepository;

    public OrderExecutionService(OrderRepository orderRepository,
                                 TransactionRepository transactionRepository,
                                 QuoteVolumeConsumptionRepository volumeRepository) {
        this.orderRepository = orderRepository;
        this.transactionRepository = transactionRepository;
        this.volumeRepository = volumeRepository;
    }

    @Transactional
    public Order createOrder(Order order) { return orderRepository.save(order); }

    @Transactional
    public void activateOrder(Long orderId) {
        Order order = lockOrder(orderId);
        if (order.getStatus() != OrderStatusEnum.PENDING) return;
        order.setStatus(OrderStatusEnum.OPEN);
        order.setUpdatedAt(Instant.now());
    }

    @Transactional
    public void rejectOrder(Long orderId) {
        Order order = lockOrder(orderId);
        order.setStatus(OrderStatusEnum.REJECTED);
        order.setUpdatedAt(Instant.now());
    }

    public List<Long> activeSecurityIds() { return orderRepository.findActiveSecurityIds(ACTIVE); }

    @Transactional
    public Transaction prepareNextFill(Long securityId, MarketQuoteDto quote, OrderSideEnum side) {
        List<Order> activeOrders = orderRepository.lockActiveOrdersForSecurity(securityId, ACTIVE).stream()
                .filter(o -> o.getSide() == side && isEligible(o, quote, side))
                .sorted(priorityComparator(side))
                .toList();
        if (activeOrders.isEmpty()) return null;

        String snapshotKey = securityId + ":" + quote.getSnapshotKey();
        volumeRepository.createSnapshotIfAbsent(snapshotKey, securityId, quote.getQuoteUpdatedAt());
        QuoteVolumeConsumption usage = volumeRepository.lockBySnapshotKey(snapshotKey)
                .orElseThrow(() -> new IllegalStateException("Не удалось создать учёт использованного объёма котировки"));
        boolean buysConsumeAsk = side == OrderSideEnum.BUY;
        Long quoteVolume = buysConsumeAsk ? quote.getAskVolume() : quote.getBidVolume();
        if (quoteVolume == null || quoteVolume <= 0) return null;
        long alreadyUsed = buysConsumeAsk ? nonNull(usage.getAskUsed()) : nonNull(usage.getBidUsed());
        long available = Math.max(0, quoteVolume - alreadyUsed);
        int lotSize = Math.max(1, Optional.ofNullable(quote.getLotSize()).orElse(1));

        for (Order order : activeOrders) {
            if (order.getRemainingQuantity() == null || order.getRemainingQuantity() <= 0) continue;
            long candidate = Math.min(order.getRemainingQuantity(), available);
            int fillQuantity = (int) (candidate - Math.floorMod(candidate, lotSize));
            if (fillQuantity <= 0) continue;
            BigDecimal price = buysConsumeAsk ? quote.getAskPrice() : quote.getBidPrice();
            if (price == null || price.signum() <= 0) return null;

            Transaction transaction = new Transaction();
            transaction.setExecutionRef(UUID.randomUUID());
            transaction.setOrder(order);
            transaction.setSide(order.getSide());
            transaction.setOrderType(order.getOrderType());
            transaction.setQuantity(fillQuantity);
            transaction.setExecutedPrice(price);
            transaction.setExecutedFee(FeeCalculator.calculateFeePerUnit(price).multiply(BigDecimal.valueOf(fillQuantity)));
            transaction.setMarketDataAt(quote.getQuoteUpdatedAt());
            transaction.setDataSource(Optional.ofNullable(quote.getSource()).orElse("UNKNOWN"));
            transaction.setStatus(TransactionStatusEnum.PENDING);
            transaction.setCreatedAt(Instant.now());
            transactionRepository.save(transaction);

            order.setRemainingQuantity(order.getRemainingQuantity() - fillQuantity);
            order.setStatus(OrderStatusEnum.PROCESSING);
            order.setUpdatedAt(Instant.now());
            if (buysConsumeAsk) usage.setAskUsed(alreadyUsed + fillQuantity);
            else usage.setBidUsed(alreadyUsed + fillQuantity);
            volumeRepository.save(usage);
            return transaction;
        }
        return null;
    }

    @Transactional
    public OrderStatusEnum completeFill(Long transactionId) {
        Transaction transaction = transactionRepository.lockById(transactionId);
        if (transaction == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Исполнение не найдено");
        if (transaction.getStatus() == TransactionStatusEnum.EXECUTED) return transaction.getOrder().getStatus();
        transaction.setStatus(TransactionStatusEnum.EXECUTED);
        transaction.setExecutedAt(Instant.now());

        Order order = lockOrder(transaction.getOrder().getId());
        OrderStatusEnum status;
        if (order.getOrderType() == OrderTypeEnum.MARKET && order.getRemainingQuantity() > 0) {
            status = OrderStatusEnum.CANCEL_PENDING;
        } else if (order.getRemainingQuantity() == 0) {
            status = OrderStatusEnum.FILLED;
        } else {
            status = OrderStatusEnum.PARTIALLY_FILLED;
        }
        order.setStatus(status);
        order.setUpdatedAt(Instant.now());
        return status;
    }

    @Transactional
    public Order beginCancellation(Long orderId, Long userId) {
        Order order = lockOrder(orderId);
        if (!order.getCustomerAccountId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Заявка не найдена");
        }
        if (order.getStatus() == OrderStatusEnum.CANCELED) return null;
        if (!ACTIVE.contains(order.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Заявку уже нельзя отменить");
        }
        order.setStatus(OrderStatusEnum.CANCEL_PENDING);
        order.setUpdatedAt(Instant.now());
        return order;
    }

    @Transactional
    public Order beginUnfilledMarketCancellation(Long orderId) {
        Order order = lockOrder(orderId);
        if (order.getOrderType() != OrderTypeEnum.MARKET || !ACTIVE.contains(order.getStatus())) return null;
        order.setStatus(OrderStatusEnum.CANCEL_PENDING);
        order.setUpdatedAt(Instant.now());
        return order;
    }

    @Transactional
    public void finishCancellation(Long orderId) {
        Order order = lockOrder(orderId);
        if (order.getStatus() != OrderStatusEnum.CANCEL_PENDING) return;
        order.setStatus(OrderStatusEnum.CANCELED);
        order.setUpdatedAt(Instant.now());
    }

    @Transactional
    public Order getOrder(Long orderId) { return orderRepository.findById(orderId).orElse(null); }

    @Transactional
    public Order getUserOrder(Long orderId, Long userId) {
        return orderRepository.findByIdAndCustomerAccountId(orderId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Заявка не найдена"));
    }

    public List<Long> pendingTransactionIds() {
        return transactionRepository.findAllByStatusOrderByCreatedAtAsc(TransactionStatusEnum.PENDING)
                .stream().map(Transaction::getId).toList();
    }

    public List<Long> pendingCancellationIds() {
        return orderRepository.findAllByStatusOrderByCreatedAtAsc(OrderStatusEnum.CANCEL_PENDING)
                .stream().map(Order::getId).toList();
    }

    private Order lockOrder(Long orderId) {
        return orderRepository.lockById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Заявка не найдена"));
    }

    private boolean isEligible(Order order, MarketQuoteDto quote, OrderSideEnum side) {
        if (order.getOrderType() == OrderTypeEnum.MARKET) return true;
        if (order.getLimitPrice() == null) return false;
        if (side == OrderSideEnum.BUY) {
            return quote.getAskPrice() != null && quote.getAskPrice().compareTo(order.getLimitPrice()) <= 0;
        }
        return quote.getBidPrice() != null && quote.getBidPrice().compareTo(order.getLimitPrice()) >= 0;
    }

    private Comparator<Order> priorityComparator(OrderSideEnum side) {
        Comparator<Order> marketFirst = Comparator.comparing(o -> o.getOrderType() == OrderTypeEnum.MARKET ? 0 : 1);
        Comparator<Order> price = side == OrderSideEnum.BUY
                ? Comparator.comparing(Order::getLimitPrice, Comparator.nullsFirst(Comparator.reverseOrder()))
                : Comparator.comparing(Order::getLimitPrice, Comparator.nullsLast(Comparator.naturalOrder()));
        return marketFirst.thenComparing(price).thenComparing(Order::getCreatedAt);
    }

    private long nonNull(Long value) { return value == null ? 0L : value; }
}
