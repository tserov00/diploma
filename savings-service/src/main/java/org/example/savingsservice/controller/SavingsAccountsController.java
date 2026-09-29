package org.example.savingsservice.controller;



import jakarta.validation.Valid;

import org.example.savingsservice.dto.request.ReserveAmountRequestDto;
import org.example.savingsservice.dto.request.SavingsAccountTransactionDto;
import org.example.savingsservice.dto.request.ExecutionSettlementRequestDto;
import org.example.savingsservice.dto.request.ExecutionCreditRequestDto;
import org.example.savingsservice.dto.response.SavingsAccountEntityResponseDto;
import org.example.savingsservice.dto.response.SavingsAccountHistoryResponseDto;
import org.example.savingsservice.dto.response.SavingsAccountResponseDto;
import org.example.savingsservice.service.SavingsAccountsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
public class SavingsAccountsController {

    private final SavingsAccountsService savingsAccountsService;

    public SavingsAccountsController(SavingsAccountsService savingsAccountsService) {
        this.savingsAccountsService = savingsAccountsService;
    }


    @GetMapping("/list")
    public ResponseEntity<List<SavingsAccountResponseDto>> getSavingsAccounts(@RequestHeader("User-Id") Long userId) {
        List<SavingsAccountResponseDto> savingsAccounts =
                savingsAccountsService.getSavingsAccounts(userId);

        return ResponseEntity.ok(savingsAccounts);
    }

    @GetMapping("/history")
    public ResponseEntity<List<SavingsAccountHistoryResponseDto>> getHistory(@RequestHeader("User-Id") Long userId) {
        List<SavingsAccountHistoryResponseDto> history =
                savingsAccountsService.getHistory(userId);

        return ResponseEntity.ok(history);
    }

    @PostMapping("/deposit")
    public ResponseEntity<Void> deposit(@Valid @RequestBody SavingsAccountTransactionDto transactionDto, @RequestHeader("User-Id") Long userId) {
        savingsAccountsService.deposit(userId, transactionDto);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/withdraw")
    public ResponseEntity<Void> withdraw(@Valid @RequestBody SavingsAccountTransactionDto transactionDto, @RequestHeader("User-Id") Long userId) {
        savingsAccountsService.withdraw(userId, transactionDto);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<SavingsAccountEntityResponseDto> getByCurrency(@RequestParam Long currencyId ,@RequestHeader("User-Id") Long userId) {
        SavingsAccountEntityResponseDto responseDto = savingsAccountsService.getSavingsAccount(currencyId, userId);

        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    @PostMapping("/reserve")
    public ResponseEntity<Void> reserveAmount(@Valid @RequestBody ReserveAmountRequestDto body, @RequestHeader("X-Request-Id") UUID requestId) {
        savingsAccountsService.reserveAmount(body.getUserId(), body.getCurrencyId(), body.getAmount(), requestId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/release")
    public ResponseEntity<Void> releaseAmount(@Valid @RequestBody ReserveAmountRequestDto body, @RequestHeader("X-Request-Id") UUID requestId) {
        savingsAccountsService.releaseAmount(body.getUserId(), body.getCurrencyId(), body.getAmount(), requestId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/settle-execution")
    public ResponseEntity<Void> settleExecution(@RequestBody ExecutionSettlementRequestDto body,
                                                @RequestHeader("X-Request-Id") UUID requestId) {
        savingsAccountsService.settleReservedExecution(body, requestId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/credit-execution")
    public ResponseEntity<Void> creditExecution(@RequestBody ExecutionCreditRequestDto body,
                                                @RequestHeader("X-Request-Id") UUID requestId) {
        savingsAccountsService.creditExecution(body.getUserId(), body.getCurrencyId(), body.getAmount(), requestId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
