package org.example.orderservice.service;

import org.example.orderservice.client.SecurityClient;
import org.example.orderservice.dto.request.SecurityDto;
import org.example.orderservice.dto.response.TransactionResponseDto;
import org.example.orderservice.entity.Transaction;
import org.example.orderservice.enumeration.TransactionStatusEnum;
import org.example.orderservice.mapper.TransactionMapper;
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
    private final TransactionMapper transactionMapper;

    public TransactionService(TransactionRepository transactionRepository,
                              SecurityClient securityClient,
                              TransactionMapper transactionMapper) {
        this.transactionRepository = transactionRepository;
        this.securityClient = securityClient;
        this.transactionMapper = transactionMapper;
    }

    public List<TransactionResponseDto> getAllTransactions(Long userId) {
        List<Transaction> transactions = transactionRepository
                .findAllByOrder_CustomerAccountIdAndStatusOrderByCreatedAtDesc(userId, TransactionStatusEnum.EXECUTED);
        List<Long> securityIds = transactions.stream().map(t -> t.getOrder().getSecurityId()).distinct().toList();
        Map<Long, SecurityDto> securities = securityClient.getByIds(securityIds).stream()
                .collect(Collectors.toMap(SecurityDto::getId, Function.identity()));
        return transactionMapper.toResponseDtos(transactions, securities);
    }
}
