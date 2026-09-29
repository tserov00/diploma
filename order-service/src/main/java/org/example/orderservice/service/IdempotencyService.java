package org.example.orderservice.service;


import org.example.orderservice.repository.ProcessedOperationsRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class IdempotencyService {

    private final ProcessedOperationsRepository repo;

    public IdempotencyService(ProcessedOperationsRepository repo) {
        this.repo = repo;
    }

    public boolean checkAndMark(UUID operationId) {
        return repo.insertIfAbsent(operationId) == 1;
    }
}
