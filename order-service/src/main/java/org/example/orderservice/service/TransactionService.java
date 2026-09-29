package org.example.orderservice.service;

import org.example.orderservice.client.SecurityClient;
import org.example.orderservice.dto.request.SecurityDto;
import org.example.orderservice.dto.response.TransactionResponseDto;
import org.example.orderservice.entity.Transaction;
import org.example.orderservice.enumeration.TransactionStatusEnum;
import org.example.orderservice.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final SecurityClient securityClient;

    public TransactionService(TransactionRepository transactionRepository, SecurityClient securityClient) {
        this.transactionRepository = transactionRepository;
        this.securityClient = securityClient;
    }

    public List<TransactionResponseDto> getAllTransactions(Long userId) {
        List<Transaction> transactions = transactionRepository
                .findAllByOrder_CustomerAccountIdAndStatusOrderByCreatedAtDesc(userId, TransactionStatusEnum.EXECUTED);
        List<Long> securityIds = transactions.stream().map(t -> t.getOrder().getSecurityId()).distinct().toList();
        Map<Long, SecurityDto> securities = securityClient.getByIds(securityIds).stream()
                .collect(Collectors.toMap(SecurityDto::getId, Function.identity()));
        return transactions.stream().map(transaction -> {
            TransactionResponseDto dto = new TransactionResponseDto();
            dto.setId(transaction.getId());
            dto.setTicker(securities.containsKey(transaction.getOrder().getSecurityId())
                    ? securities.get(transaction.getOrder().getSecurityId()).getTicker() : "-");
            dto.setSide(transaction.getSide().name());
            dto.setOrderType(transaction.getOrderType().name());
            dto.setExecutedPrice(transaction.getExecutedPrice());
            dto.setExecutedFee(transaction.getExecutedFee());
            dto.setQuantity(transaction.getQuantity());
            dto.setOrderId(transaction.getOrder().getId());
            dto.setMarketDataAt(transaction.getMarketDataAt());
            dto.setExecutedAt(transaction.getExecutedAt());
            dto.setDataSource(transaction.getDataSource());
            return dto;
        }).toList();
    }
}
