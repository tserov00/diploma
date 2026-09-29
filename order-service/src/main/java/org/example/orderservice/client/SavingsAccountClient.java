package org.example.orderservice.client;

import org.example.orderservice.dto.request.SavingsAccountDto;
import org.example.orderservice.dto.request.ReserveAmountRequestDto;
import org.example.orderservice.dto.request.ExecutionSettlementRequestDto;
import org.example.orderservice.dto.request.ExecutionCreditRequestDto;
import org.example.orderservice.dto.response.SavingsAccountTransactionDto;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.UUID;

@Service
public class SavingsAccountClient {
    private final WebClient savingsWebClient;

    public SavingsAccountClient(WebClient savingsWebClient) {
        this.savingsWebClient = savingsWebClient;
    }

    public SavingsAccountDto getByCurrency(Long userId, Long currencyId) {
        return savingsWebClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/accounts")
                        .queryParam("currencyId", currencyId)
                        .build()
                )
                .header("User-Id", String.valueOf(userId))
                .retrieve()
                .bodyToMono(SavingsAccountDto.class)
                .block();
    }

    public ResponseEntity<Void> reserveAmount(ReserveAmountRequestDto dto, UUID requestId) {
        return savingsWebClient.post()
                .uri("/api/accounts/reserve")
                .bodyValue(dto)
                .header("X-Request-Id", String.valueOf(requestId))
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    public ResponseEntity<Void> releaseAmount(ReserveAmountRequestDto dto, UUID requestId) {
        return savingsWebClient.post()
                .uri("/api/accounts/release")
                .bodyValue(dto)
                .header("X-Request-Id", String.valueOf(requestId))
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    public ResponseEntity<Void> deposit(SavingsAccountTransactionDto transactionDto, Long userId) {
        return savingsWebClient.post()
                .uri("/api/accounts/deposit")
                .bodyValue(transactionDto)
                .header("User-Id", String.valueOf(userId))
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    public ResponseEntity<Void> withdraw(SavingsAccountTransactionDto transactionDto, Long userId) {
        return savingsWebClient.post()
                .uri("/api/accounts/withdraw")
                .bodyValue(transactionDto)
                .header("User-Id", String.valueOf(userId))
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    public ResponseEntity<Void> settleReservedExecution(ExecutionSettlementRequestDto dto, UUID requestId) {
        return savingsWebClient.post()
                .uri("/api/accounts/settle-execution")
                .bodyValue(dto)
                .header("X-Request-Id", String.valueOf(requestId))
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    public ResponseEntity<Void> creditExecution(ExecutionCreditRequestDto dto, UUID requestId) {
        return savingsWebClient.post()
                .uri("/api/accounts/credit-execution")
                .bodyValue(dto)
                .header("X-Request-Id", String.valueOf(requestId))
                .retrieve()
                .toBodilessEntity()
                .block();
    }
}
