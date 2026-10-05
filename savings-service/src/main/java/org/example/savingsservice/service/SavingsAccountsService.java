package org.example.savingsservice.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.example.savingsservice.dto.request.ExecutionSettlementRequestDto;
import org.example.savingsservice.dto.request.SavingsAccountTransactionDto;
import org.example.savingsservice.dto.response.SavingsAccountEntityResponseDto;
import org.example.savingsservice.dto.response.SavingsAccountHistoryResponseDto;
import org.example.savingsservice.dto.response.SavingsAccountResponseDto;
import org.example.savingsservice.entity.BalanceHistory;
import org.example.savingsservice.entity.Currency;
import org.example.savingsservice.entity.SavingsAccount;
import org.example.savingsservice.enumeration.CurrencyCodeEnum;
import org.example.savingsservice.enumeration.TransactionTypeEnum;
import org.example.savingsservice.exception.custom.InsufficientBalanceException;
import org.example.savingsservice.exception.custom.MissingCurrencyException;
import org.example.savingsservice.mapper.BalanceHistoryMapper;
import org.example.savingsservice.mapper.SavingsAccountMapper;
import org.example.savingsservice.repository.BalanceHistoryRepository;
import org.example.savingsservice.repository.CurrencyRepository;
import org.example.savingsservice.repository.SavingsAccountRepository;
import org.example.savingsservice.repository.TransactionTypeRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class SavingsAccountsService {
    @PersistenceContext
    private final EntityManager entityManager;
    private final SavingsAccountRepository savingsAccountRepository;
    private final CurrencyRepository currencyRepository;
    private final BalanceHistoryRepository balanceHistoryRepository;
    private final TransactionTypeRepository transactionTypeRepository;
    private final SavingsAccountMapper savingsAccountMapper;
    private final BalanceHistoryMapper balanceHistoryMapper;
    private final IdempotencyService idempotencyService;

    public SavingsAccountsService(SavingsAccountRepository savingsAccountRepository,
                                  CurrencyRepository currencyRepository,
                                  BalanceHistoryRepository balanceHistoryRepository,
                                  TransactionTypeRepository transactionTypeRepository,
                                  SavingsAccountMapper savingsAccountMapper,
                                  BalanceHistoryMapper balanceHistoryMapper,
                                  IdempotencyService idempotencyService,
                                  EntityManager entityManager) {
        this.savingsAccountRepository = savingsAccountRepository;
        this.currencyRepository = currencyRepository;
        this.balanceHistoryRepository = balanceHistoryRepository;
        this.transactionTypeRepository = transactionTypeRepository;
        this.savingsAccountMapper = savingsAccountMapper;
        this.balanceHistoryMapper = balanceHistoryMapper;
        this.idempotencyService = idempotencyService;
        this.entityManager = entityManager;
    }

    @Transactional
    public List<SavingsAccountResponseDto> getSavingsAccounts(Long userId) {
        ensureRubleAccount(userId);
        return savingsAccountRepository.findAllByAccountId(userId).stream()
                .map(savingsAccountMapper::toResponseDto).toList();
    }

    @Transactional
    public List<SavingsAccountHistoryResponseDto> getHistory(Long userId) {
        ensureRubleAccount(userId);
        List<SavingsAccount> accounts = savingsAccountRepository.findAllByAccountId(userId);
        return balanceHistoryRepository.findAllBySavingsAccountIn(accounts).stream()
                .map(balanceHistoryMapper::toResponseDto).toList();
    }

    @Transactional
    public void deposit(Long userId, SavingsAccountTransactionDto transactionDto) {
        Currency currency = findCurrency(transactionDto.getCurrencyCode());
        entityManager.createNativeQuery("CALL deposit_balance(:p_amount, :p_currency_id, :p_customer_account_id)")
                .setParameter("p_amount", transactionDto.getAmount())
                .setParameter("p_currency_id", currency.getId().intValue())
                .setParameter("p_customer_account_id", userId.intValue())
                .executeUpdate();
    }

    @Transactional
    public void withdraw(Long userId, SavingsAccountTransactionDto transactionDto) {
        Currency currency = findCurrency(transactionDto.getCurrencyCode());
        SavingsAccount account = savingsAccountRepository.lockByCurrencyAndAccount(currency.getId(), userId)
                .orElseThrow(() -> new MissingCurrencyException("У вас нет счета с нужной валютой"));
        BigDecimal freeAmount = account.getBalance().subtract(account.getReservedAmount());
        if (freeAmount.compareTo(transactionDto.getAmount()) < 0) {
            throw new InsufficientBalanceException("Недостаточно средств");
        }
        entityManager.createNativeQuery("CALL withdraw_balance(:p_amount, :p_currency_id, :p_customer_account_id)")
                .setParameter("p_amount", transactionDto.getAmount())
                .setParameter("p_currency_id", currency.getId().intValue())
                .setParameter("p_customer_account_id", userId.intValue())
                .executeUpdate();
    }

    @Transactional
    public void reserveAmount(Long userId, Long currencyId, BigDecimal amount, UUID requestId) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Сумма резерва должна быть больше нуля");
        }
        if (!idempotencyService.checkAndMark(requestId)) return;
        SavingsAccount account = savingsAccountRepository.lockByCurrencyAndAccount(currencyId, userId)
                .orElseThrow(() -> new MissingCurrencyException("У вас нет счета с нужной валютой"));
        BigDecimal newReservedAmount = account.getReservedAmount().add(amount);
        if (newReservedAmount.compareTo(account.getBalance()) > 0) {
            throw new InsufficientBalanceException("Недостаточно средств");
        }
        account.setReservedAmount(newReservedAmount);
        savingsAccountRepository.save(account);
    }

    @Transactional
    public void releaseAmount(Long userId, Long currencyId, BigDecimal amount, UUID requestId) {
        if (!idempotencyService.checkAndMark(requestId)) return;
        SavingsAccount account = savingsAccountRepository.lockByCurrencyAndAccount(currencyId, userId)
                .orElseThrow(() -> new MissingCurrencyException("У вас нет счета с нужной валютой"));
        if (amount == null || amount.signum() < 0 || account.getReservedAmount().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Недостаточно зарезервированных средств");
        }
        account.setReservedAmount(account.getReservedAmount().subtract(amount));
        savingsAccountRepository.save(account);
    }

    @Transactional
    public void settleReservedExecution(ExecutionSettlementRequestDto dto, UUID requestId) {
        if (!idempotencyService.checkAndMark(requestId)) return;
        validatePositive(dto.getReservedAmount(), "Сумма снятия резерва");
        if (dto.getDebitAmount() == null || dto.getDebitAmount().signum() < 0
                || dto.getDebitAmount().compareTo(dto.getReservedAmount()) > 0) {
            throw new IllegalArgumentException("Сумма списания превышает резерв");
        }
        SavingsAccount account = savingsAccountRepository.lockByCurrencyAndAccount(dto.getCurrencyId(), dto.getUserId())
                .orElseThrow(() -> new MissingCurrencyException("У вас нет счета с нужной валютой"));
        if (account.getReservedAmount().compareTo(dto.getReservedAmount()) < 0
                || account.getBalance().compareTo(dto.getDebitAmount()) < 0) {
            throw new InsufficientBalanceException("Недостаточно зарезервированных средств для исполнения");
        }
        account.setReservedAmount(account.getReservedAmount().subtract(dto.getReservedAmount()));
        account.setBalance(account.getBalance().subtract(dto.getDebitAmount()));
        savingsAccountRepository.save(account);
        if (dto.getDebitAmount().signum() > 0) {
            balanceHistoryRepository.save(new BalanceHistory(account, LocalDateTime.now(), dto.getDebitAmount(),
                    transactionTypeRepository.findByTransactionType(TransactionTypeEnum.BUY).orElseThrow()));
        }
    }

    @Transactional
    public void creditExecution(Long userId, Long currencyId, BigDecimal amount, UUID requestId) {
        if (!idempotencyService.checkAndMark(requestId)) return;
        validatePositive(amount, "Сумма продажи");
        SavingsAccount account = savingsAccountRepository.lockByCurrencyAndAccount(currencyId, userId)
                .orElseThrow(() -> new MissingCurrencyException("У вас нет счета с нужной валютой"));
        account.setBalance(account.getBalance().add(amount));
        savingsAccountRepository.save(account);
        balanceHistoryRepository.save(new BalanceHistory(account, LocalDateTime.now(), amount,
                transactionTypeRepository.findByTransactionType(TransactionTypeEnum.SELL).orElseThrow()));
    }

    @Transactional
    public SavingsAccountEntityResponseDto getSavingsAccount(Long currencyId, Long accountId) {
        if (CurrencyCodeEnum.RUB.getId().equals(currencyId)) ensureRubleAccount(accountId);
        SavingsAccount account = savingsAccountRepository.findByCurrencyIdAndAccountId(currencyId, accountId).orElse(null);
        return savingsAccountMapper.toEntityResponseDto(account);
    }

    private Currency findCurrency(String code) {
        CurrencyCodeEnum currencyEnum;
        try {
            currencyEnum = CurrencyCodeEnum.valueOf(code.toUpperCase());
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Неверный код валюты");
        }
        return currencyRepository.findByCode(currencyEnum)
                .orElseThrow(() -> new MissingCurrencyException("Валюта не найдена в базе данных"));
    }

    private SavingsAccount ensureRubleAccount(Long userId) {
        return savingsAccountRepository.findByCurrencyIdAndAccountId(CurrencyCodeEnum.RUB.getId(), userId)
                .orElseGet(() -> savingsAccountRepository.save(new SavingsAccount(
                        userId,
                        "SIM-RUB-" + userId + "-" + UUID.randomUUID().toString().substring(0, 8),
                        CurrencyCodeEnum.RUB.getId(),
                        BigDecimal.ZERO,
                        BigDecimal.ZERO)));
    }

    private void validatePositive(BigDecimal amount, String label) {
        if (amount == null || amount.signum() <= 0) throw new IllegalArgumentException(label + " должна быть больше нуля");
    }
}
