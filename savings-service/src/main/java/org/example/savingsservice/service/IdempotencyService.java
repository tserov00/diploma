package org.example.savingsservice.service;

import org.example.savingsservice.repository.ProcessedOperationsRepository;
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
