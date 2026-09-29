package org.example.orderservice.service;

import jakarta.transaction.Transactional;
import org.example.orderservice.client.SecurityClient;
import org.example.orderservice.dto.request.SecurityDto;
import org.example.orderservice.dto.response.PortfolioResponseDto;
import org.example.orderservice.entity.CustomerPortfolio;
import org.example.orderservice.exception.custom.InsufficientSecurityException;
import org.example.orderservice.mapper.CustomerPortfolioMapper;
import org.example.orderservice.repository.CustomerPortfolioRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CustomerPortfolioService {
    private final CustomerPortfolioMapper portfolioMapper;
    private final SecurityClient securityClient;
    private final CustomerPortfolioRepository portfolioRepository;
    private final IdempotencyService idempotencyService;

    public CustomerPortfolioService(CustomerPortfolioMapper portfolioMapper,
                                    SecurityClient securityClient,
                                    CustomerPortfolioRepository portfolioRepository,
                                    IdempotencyService idempotencyService) {
        this.portfolioMapper = portfolioMapper;
        this.securityClient = securityClient;
        this.portfolioRepository = portfolioRepository;
        this.idempotencyService = idempotencyService;
    }

    public List<PortfolioResponseDto> getPortfolio(Long userId) {
        List<CustomerPortfolio> portfolios = portfolioRepository.findAllByCustomerAccountId(userId);
        if (portfolios.isEmpty()) return Collections.emptyList();
        List<Long> securityIds = portfolios.stream().map(CustomerPortfolio::getSecurityId).distinct().toList();
        Map<Long, SecurityDto> securities = securityClient.getByIds(securityIds).stream()
                .collect(Collectors.toMap(SecurityDto::getId, s -> s));
        List<PortfolioResponseDto> result = new ArrayList<>();
        for (CustomerPortfolio portfolio : portfolios) {
            SecurityDto security = securities.get(portfolio.getSecurityId());
            if (security == null) continue;
            PortfolioResponseDto dto = portfolioMapper.toResponseDto(portfolio, security);
            dto.setReservedQuantity(portfolio.getReservedQuantity());
            BigDecimal profit = security.getLastPrice() == null ? null
                    : security.getLastPrice().subtract(Optional.ofNullable(portfolio.getAvgBuyPrice()).orElse(BigDecimal.ZERO))
                    .multiply(BigDecimal.valueOf(portfolio.getTotalQuantity()))
                    .setScale(2, RoundingMode.HALF_UP);
            dto.setUnrealizedProfit(profit);
            result.add(dto);
        }
        return result;
    }

    @Transactional
    public void reserveShares(Long userId, Long securityId, int quantity, UUID operationId) {
        if (!idempotencyService.checkAndMark(operationId)) return;
        CustomerPortfolio portfolio = portfolioRepository.lockByCustomerAndSecurity(userId, securityId)
                .orElseThrow(() -> new InsufficientSecurityException("Недостаточно ценных бумаг для продажи"));
        int total = nonNull(portfolio.getTotalQuantity());
        int reserved = nonNull(portfolio.getReservedQuantity());
        if (total - reserved < quantity) throw new InsufficientSecurityException("Недостаточно свободных бумаг для продажи");
        portfolio.setReservedQuantity(reserved + quantity);
        portfolioRepository.save(portfolio);
    }

    @Transactional
    public void releaseReservedShares(Long userId, Long securityId, int quantity, UUID operationId) {
        if (quantity <= 0 || !idempotencyService.checkAndMark(operationId)) return;
        CustomerPortfolio portfolio = portfolioRepository.lockByCustomerAndSecurity(userId, securityId)
                .orElseThrow(() -> new InsufficientSecurityException("Портфель не найден"));
        int reserved = nonNull(portfolio.getReservedQuantity());
        if (reserved < quantity) throw new InsufficientSecurityException("Недостаточно зарезервированных бумаг");
        portfolio.setReservedQuantity(reserved - quantity);
        portfolioRepository.save(portfolio);
    }

    @Transactional
    public void applyBuyExecution(Long userId, Long securityId, int quantity,
                                  BigDecimal executedPriceWithFee, UUID operationId) {
        if (!idempotencyService.checkAndMark(operationId)) return;
        CustomerPortfolio portfolio = portfolioRepository.lockByCustomerAndSecurity(userId, securityId)
                .orElseGet(() -> {
                    CustomerPortfolio created = new CustomerPortfolio();
                    created.setCustomerAccountId(userId);
                    created.setSecurityId(securityId);
                    created.setTotalQuantity(0);
                    created.setReservedQuantity(0);
                    created.setSoldQuantity(0);
                    created.setAvgBuyPrice(BigDecimal.ZERO);
                    created.setAvgSellPrice(BigDecimal.ZERO);
                    return created;
                });
        int previousQuantity = nonNull(portfolio.getTotalQuantity());
        BigDecimal previousAverage = Optional.ofNullable(portfolio.getAvgBuyPrice()).orElse(BigDecimal.ZERO);
        int newQuantity = previousQuantity + quantity;
        BigDecimal weightedAverage = previousQuantity == 0
                ? executedPriceWithFee
                : previousAverage.multiply(BigDecimal.valueOf(previousQuantity))
                .add(executedPriceWithFee.multiply(BigDecimal.valueOf(quantity)))
                .divide(BigDecimal.valueOf(newQuantity), 8, RoundingMode.HALF_UP);
        portfolio.setTotalQuantity(newQuantity);
        portfolio.setAvgBuyPrice(weightedAverage);
        portfolioRepository.save(portfolio);
    }

    @Transactional
    public void applySellExecution(Long userId, Long securityId, int quantity,
                                   BigDecimal executedPriceAfterFee, UUID operationId) {
        if (!idempotencyService.checkAndMark(operationId)) return;
        CustomerPortfolio portfolio = portfolioRepository.lockByCustomerAndSecurity(userId, securityId)
                .orElseThrow(() -> new InsufficientSecurityException("Портфель не найден"));
        int total = nonNull(portfolio.getTotalQuantity());
        int reserved = nonNull(portfolio.getReservedQuantity());
        if (total < quantity || reserved < quantity) {
            throw new InsufficientSecurityException("Недостаточно зарезервированных бумаг для исполнения");
        }
        int soldBefore = nonNull(portfolio.getSoldQuantity());
        BigDecimal avgSellBefore = Optional.ofNullable(portfolio.getAvgSellPrice()).orElse(BigDecimal.ZERO);
        int soldAfter = soldBefore + quantity;
        BigDecimal avgSellAfter = soldBefore == 0
                ? executedPriceAfterFee
                : avgSellBefore.multiply(BigDecimal.valueOf(soldBefore))
                .add(executedPriceAfterFee.multiply(BigDecimal.valueOf(quantity)))
                .divide(BigDecimal.valueOf(soldAfter), 8, RoundingMode.HALF_UP);
        portfolio.setTotalQuantity(total - quantity);
        portfolio.setReservedQuantity(reserved - quantity);
        portfolio.setSoldQuantity(soldAfter);
        portfolio.setAvgSellPrice(avgSellAfter);
        portfolioRepository.save(portfolio);
    }

    private int nonNull(Integer value) { return value == null ? 0 : value; }
}
