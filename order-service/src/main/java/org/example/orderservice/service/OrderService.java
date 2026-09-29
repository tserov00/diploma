package org.example.orderservice.service;

import org.example.orderservice.client.SavingsAccountClient;
import org.example.orderservice.client.SecurityClient;
import org.example.orderservice.dto.request.MarketQuoteDto;
import org.example.orderservice.dto.request.OrderDto;
import org.example.orderservice.dto.request.ReserveAmountRequestDto;
import org.example.orderservice.dto.request.SavingsAccountDto;
import org.example.orderservice.dto.request.SecurityDto;
import org.example.orderservice.dto.response.OrderResponseDto;
import org.example.orderservice.entity.Order;
import org.example.orderservice.enumeration.CurrencyCodeEnum;
import org.example.orderservice.enumeration.OrderSideEnum;
import org.example.orderservice.enumeration.OrderStatusEnum;
import org.example.orderservice.enumeration.OrderTypeEnum;
import org.example.orderservice.repository.OrderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OrderService {
    private final SavingsAccountClient savingsAccountClient;
    private final SecurityClient securityClient;
    private final OrderRepository orderRepository;
    private final CustomerPortfolioService portfolioService;
    private final OrderExecutionService executionService;
    private final FillSettlementService settlementService;
    private final OrderProcessor orderProcessor;

    public OrderService(SavingsAccountClient savingsAccountClient,
                        SecurityClient securityClient,
                        OrderRepository orderRepository,
                        CustomerPortfolioService portfolioService,
                        OrderExecutionService executionService,
                        FillSettlementService settlementService,
                        OrderProcessor orderProcessor) {
        this.savingsAccountClient = savingsAccountClient;
        this.securityClient = securityClient;
        this.orderRepository = orderRepository;
        this.portfolioService = portfolioService;
        this.executionService = executionService;
        this.settlementService = settlementService;
        this.orderProcessor = orderProcessor;
    }

    public void createOrder(OrderDto dto, Long userId) {
        validateRequest(dto);
        SecurityDto security = securityClient.getById(dto.getSecurityId());
        if (security == null || security.getCurrencyCode() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Не удалось определить валюту инструмента");
        }
        Long currencyId = CurrencyCodeEnum.getIdFromCode(security.getCurrencyCode());
        SavingsAccountDto account = savingsAccountClient.getByCurrency(userId, currencyId);
        if (account == null || account.getId() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Для валюты инструмента нет счета");
        }

        MarketQuoteDto quote = securityClient.getQuoteById(security.getId());
        if (dto.getOrderType() == OrderTypeEnum.MARKET) validateMarketQuote(quote, dto.getSide());
        int lotSize = Math.max(1, quote != null && quote.getLotSize() != null
                ? quote.getLotSize() : security.getLotSize() == null ? 1 : security.getLotSize());
        if (dto.getQuantity() % lotSize != 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Количество должно быть кратно размеру лота: " + lotSize);
        }

        BigDecimal reservePrice = dto.getOrderType() == OrderTypeEnum.LIMIT
                ? dto.getLimitPrice()
                : dto.getSide() == OrderSideEnum.BUY ? quote.getAskPrice() : quote.getBidPrice();
        BigDecimal reservePerUnit = reservePrice.add(FeeCalculator.calculateFeePerUnit(reservePrice));

        Order order = new Order();
        order.setCustomerAccountId(userId);
        order.setSecurityId(security.getId());
        order.setSavingsAccountId(account.getId());
        order.setCurrencyId(currencyId);
        order.setSide(dto.getSide());
        order.setOrderType(dto.getOrderType());
        order.setLimitPrice(dto.getOrderType() == OrderTypeEnum.LIMIT ? dto.getLimitPrice() : null);
        order.setQuantity(dto.getQuantity());
        order.setRemainingQuantity(dto.getQuantity());
        order.setReservePerUnit(dto.getSide() == OrderSideEnum.BUY ? reservePerUnit : null);
        order.setStatus(OrderStatusEnum.PENDING);
        order.setCreatedAt(Instant.now());
        order.setUpdatedAt(order.getCreatedAt());
        Order saved = executionService.createOrder(order);

        try {
            if (dto.getSide() == OrderSideEnum.BUY) {
                BigDecimal reserveAmount = reservePerUnit.multiply(BigDecimal.valueOf(dto.getQuantity()));
                ReserveAmountRequestDto reserveRequest = new ReserveAmountRequestDto(userId, currencyId, reserveAmount);
                UUID reserveOperation = orderOperationId(saved.getId(), "reserve");
                savingsAccountClient.reserveAmount(
                        reserveRequest, reserveOperation);
                executionService.activateOrder(saved.getId());
            } else {
                UUID reserveOperation = orderOperationId(saved.getId(), "reserve-shares");
                portfolioService.reserveShares(userId, security.getId(), dto.getQuantity(), reserveOperation);
                executionService.activateOrder(saved.getId());
            }
        } catch (Exception e) {
            try {
                if (dto.getSide() == OrderSideEnum.BUY) {
                    BigDecimal reserveAmount = reservePerUnit.multiply(BigDecimal.valueOf(dto.getQuantity()));
                    ReserveAmountRequestDto reserveRequest = new ReserveAmountRequestDto(userId, currencyId, reserveAmount);
                    // Retry with the same idempotency key to resolve a lost response before releasing funds.
                    savingsAccountClient.reserveAmount(reserveRequest, orderOperationId(saved.getId(), "reserve"));
                    savingsAccountClient.releaseAmount(
                            reserveRequest, orderOperationId(saved.getId(), "reserve-compensation"));
                } else {
                    portfolioService.reserveShares(userId, security.getId(), dto.getQuantity(), orderOperationId(saved.getId(), "reserve-shares"));
                    portfolioService.releaseReservedShares(userId, security.getId(), dto.getQuantity(),
                            orderOperationId(saved.getId(), "shares-compensation"));
                }
            } catch (Exception ignored) {
                // A failed reserve is rolled back; a successful or response-lost reserve is safely retried idempotently above.
            }
            executionService.rejectOrder(saved.getId());
            throw e;
        }

        if (orderProcessor.isUsable(quote)) {
            orderProcessor.processQuote(security.getId(), quote);
        }
        if (dto.getOrderType() == OrderTypeEnum.MARKET) {
            settlementService.cancelUnfilledMarketOrder(saved.getId());
        }
    }

    public void cancelOrder(Long orderId, Long userId) {
        settlementService.cancelLimitOrder(orderId, userId);
    }

    public List<OrderResponseDto> getHistory(Long userId) {
        List<Order> orders = orderRepository.findAllByCustomerAccountIdOrderByCreatedAtDesc(userId);
        List<Long> securityIds = orders.stream().map(Order::getSecurityId).distinct().toList();
        Map<Long, SecurityDto> securities = securityClient.getByIds(securityIds).stream()
                .collect(Collectors.toMap(SecurityDto::getId, Function.identity()));
        return orders.stream().map(order -> {
            SecurityDto security = securities.get(order.getSecurityId());
            OrderResponseDto dto = new OrderResponseDto();
            dto.setId(order.getId());
            dto.setTicker(security == null ? "-" : security.getTicker());
            dto.setSide(order.getSide().name());
            dto.setOrderType(order.getOrderType().name());
            dto.setLimitPrice(order.getLimitPrice());
            dto.setMarketPrice(security == null ? null : security.getLastPrice());
            dto.setQuantity(order.getQuantity());
            dto.setRemainingQuantity(order.getRemainingQuantity());
            dto.setFilledQuantity(order.getQuantity() - order.getRemainingQuantity());
            dto.setStatus(order.getStatus().name());
            dto.setCreatedAt(order.getCreatedAt());
            return dto;
        }).toList();
    }

    private void validateRequest(OrderDto dto) {
        if (dto.getSide() == null || dto.getOrderType() == null || dto.getSecurityId() == null
                || dto.getQuantity() == null || dto.getQuantity() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Некорректные параметры заявки");
        }
        if (dto.getOrderType() == OrderTypeEnum.LIMIT) {
            if (dto.getLimitPrice() == null || dto.getLimitPrice().signum() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Для лимитной заявки нужна положительная цена");
            }
        } else if (dto.getLimitPrice() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "У рыночной заявки не задаётся лимитная цена");
        }
    }

    private void validateMarketQuote(MarketQuoteDto quote, OrderSideEnum side) {
        if (!orderProcessor.isUsable(quote)) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Нет свежей котировки для рыночной заявки; попробуйте позже");
        }
        BigDecimal price = side == OrderSideEnum.BUY ? quote.getAskPrice() : quote.getBidPrice();
        Long volume = side == OrderSideEnum.BUY ? quote.getAskVolume() : quote.getBidVolume();
        if (price == null || price.signum() <= 0 || volume == null || volume <= 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "На лучшей рыночной цене нет доступного объёма");
        }
    }

    private UUID orderOperationId(Long orderId, String suffix) {
        return UUID.nameUUIDFromBytes(("order:" + orderId + ":" + suffix).getBytes(StandardCharsets.UTF_8));
    }
}
