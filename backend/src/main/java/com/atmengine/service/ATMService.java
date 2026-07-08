package com.atmengine.service;

import com.atmengine.constants.AppConstants;
import com.atmengine.constants.TransactionType;
import com.atmengine.dto.*;
import com.atmengine.entity.Account;
import com.atmengine.entity.Transaction;
import com.atmengine.exception.*;
import com.atmengine.hardware.ATMHardware;
import com.atmengine.repository.AccountRepository;
import com.atmengine.repository.TransactionRepository;
import com.atmengine.util.TransactionIdGenerator;
import com.atmengine.validation.ATMValidator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ATMService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final ATMHardware atmHardware;
    private final ATMValidator validator;
    private final TransactionIdGenerator idGenerator;
    private final AuditService auditService;

    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        log.info("ATMService: Login attempt for account: {}", maskAccount(request.getAccountNumber()));

        Account account = accountRepository.findByAccountNumber(request.getAccountNumber())
                .orElseThrow(() -> {
                    auditService.logEvent("LOGIN_FAILED", request.getAccountNumber(), null,
                            "Account not found", false, "Invalid account number", httpRequest);
                    return new InvalidPinException("Invalid account number or PIN");
                });

        validateAccountAccess(account);

        if (!account.getPin().equals(request.getPin())) {
            handleFailedLogin(account, httpRequest);
        }

        account.setFailedAttempts(0);
        accountRepository.save(account);

        String token = idGenerator.generateTransactionId();

        auditService.logEvent("LOGIN_SUCCESS", request.getAccountNumber(), account.getAccountHolderName(),
                "User logged in successfully", true, null, httpRequest);

        log.info("ATMService: Login successful for account: {}", maskAccount(request.getAccountNumber()));

        return LoginResponse.builder()
                .token(token)
                .accountNumber(account.getAccountNumber())
                .accountHolderName(account.getAccountHolderName())
                .message("Login successful")
                .success(true)
                .build();
    }

    @Transactional
    public TransactionResponse withdraw(WithdrawRequest request, HttpServletRequest httpRequest) {
        log.info("ATMService: Withdrawal attempt for account: {}, amount: {}",
                maskAccount(request.getAccountNumber()), request.getAmount());

        Account account = accountRepository.findByAccountNumberWithLock(request.getAccountNumber())
                .orElseThrow(() -> new InvalidPinException("Invalid account number or PIN"));

        validateAccountAccess(account);
        validatePinMatch(account, request.getPin());
        validator.validateWithdrawalAmount(request.getAmount());

        checkDailyLimit(account, request.getAmount());

        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            auditService.logEvent("WITHDRAWAL_FAILED", account.getAccountNumber(),
                    account.getAccountHolderName(),
                    String.format("Insufficient funds. Available: %.2f, Requested: %.2f",
                            account.getBalance(), request.getAmount()),
                    false, "Insufficient funds", httpRequest);
            throw new InsufficientFundsException(
                    "Insufficient funds in account",
                    account.getBalance().doubleValue(),
                    request.getAmount().doubleValue()
            );
        }

        double cashAmount = request.getAmount().doubleValue();
        if (cashAmount > atmHardware.getAvailableCash()) {
            auditService.logEvent("WITHDRAWAL_FAILED", account.getAccountNumber(),
                    account.getAccountHolderName(),
                    "ATM has insufficient cash", false, "ATM cash low", httpRequest);
            throw new CashDispenseException(
                    "ATM has insufficient cash to process this transaction. Please try a smaller amount.",
                    cashAmount, "ATM cash low"
            );
        }

        BigDecimal balanceBefore = account.getBalance();
        account.setBalance(account.getBalance().subtract(request.getAmount()));
        updateDailyWithdrawalTotal(account, request.getAmount());
        accountRepository.save(account);

        Transaction transaction = createTransaction(account, TransactionType.WITHDRAWAL,
                request.getAmount(), balanceBefore, account.getBalance(), null, "Withdrawal");

        try {
            atmHardware.dispenseCash(cashAmount);

            String receipt = atmHardware.generateReceipt(
                    transaction.getTransactionId(), "WITHDRAWAL",
                    request.getAmount().doubleValue(),
                    balanceBefore.doubleValue(),
                    account.getBalance().doubleValue(),
                    account.getAccountNumber()
            );
            atmHardware.printReceipt(receipt);

            transaction.setSuccessful(true);
            transactionRepository.save(transaction);

            auditService.logEvent("WITHDRAWAL_SUCCESS", account.getAccountNumber(),
                    account.getAccountHolderName(),
                    String.format("Withdrawal of %.2f successful", request.getAmount()),
                    true, null, httpRequest);

            log.info("ATMService: Withdrawal successful for account: {}, amount: {}",
                    maskAccount(request.getAccountNumber()), request.getAmount());

        } catch (Exception e) {
            log.error("ATMService: Withdrawal failed after debit. Initiating rollback for transaction: {}",
                    transaction.getTransactionId());

            account.setBalance(account.getBalance().add(request.getAmount()));
            accountRepository.save(account);

            transaction.setSuccessful(false);
            transaction.setFailureReason(e.getMessage());
            transaction.setRolledBack(true);
            transaction.setRollbackReason("Cash dispense failed. Balance restored.");
            transactionRepository.save(transaction);

            auditService.logEvent("WITHDRAWAL_ROLLBACK", account.getAccountNumber(),
                    account.getAccountHolderName(),
                    String.format("Rollback of %.2f after dispense failure", request.getAmount()),
                    true, null, httpRequest);

            throw new TransactionRollbackException(
                    "Transaction failed but has been rolled back. " + e.getMessage(),
                    transaction.getTransactionId(),
                    request.getAmount().doubleValue()
            );
        }

        return buildTransactionResponse(transaction);
    }

    @Transactional
    public TransactionResponse deposit(DepositRequest request, HttpServletRequest httpRequest) {
        log.info("ATMService: Deposit attempt for account: {}, amount: {}",
                maskAccount(request.getAccountNumber()), request.getAmount());

        Account account = accountRepository.findByAccountNumberWithLock(request.getAccountNumber())
                .orElseThrow(() -> new InvalidPinException("Account not found"));

        validateAccountAccess(account);
        validator.validateDepositAmount(request.getAmount());

        BigDecimal balanceBefore = account.getBalance();
        account.setBalance(account.getBalance().add(request.getAmount()));
        accountRepository.save(account);

        Transaction transaction = createTransaction(account, TransactionType.DEPOSIT,
                request.getAmount(), balanceBefore, account.getBalance(), null, "Deposit");
        transaction.setSuccessful(true);
        transactionRepository.save(transaction);

        auditService.logEvent("DEPOSIT_SUCCESS", account.getAccountNumber(),
                account.getAccountHolderName(),
                String.format("Deposit of %.2f successful", request.getAmount()),
                true, null, httpRequest);

        log.info("ATMService: Deposit successful for account: {}, amount: {}",
                maskAccount(request.getAccountNumber()), request.getAmount());

        return buildTransactionResponse(transaction);
    }

    @Transactional
    public TransactionResponse transfer(TransferRequest request, HttpServletRequest httpRequest) {
        log.info("ATMService: Transfer attempt from: {} to: {}, amount: {}",
                maskAccount(request.getFromAccount()),
                maskAccount(request.getToAccount()),
                request.getAmount());

        validator.validateTransferAmount(request.getAmount());

        Account fromAccount = accountRepository.findByAccountNumberWithLock(request.getFromAccount())
                .orElseThrow(() -> new InvalidPinException("Invalid source account"));

        Account toAccount = accountRepository.findByAccountNumberWithLock(request.getToAccount())
                .orElseThrow(() -> new InvalidPinException("Invalid target account"));

        validateAccountAccess(fromAccount);
        validatePinMatch(fromAccount, request.getPin());

        if (fromAccount.getAccountNumber().equals(toAccount.getAccountNumber())) {
            throw new InvalidAmountException("Cannot transfer to the same account", request.getAmount().doubleValue());
        }

        if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException(
                    "Insufficient funds for transfer",
                    fromAccount.getBalance().doubleValue(),
                    request.getAmount().doubleValue()
            );
        }

        BigDecimal fromBalanceBefore = fromAccount.getBalance();
        BigDecimal toBalanceBefore = toAccount.getBalance();

        fromAccount.setBalance(fromAccount.getBalance().subtract(request.getAmount()));
        toAccount.setBalance(toAccount.getBalance().add(request.getAmount()));

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        Transaction debitTransaction = createTransaction(fromAccount, TransactionType.TRANSFER,
                request.getAmount(), fromBalanceBefore, fromAccount.getBalance(),
                toAccount.getAccountNumber(),
                request.getDescription() != null ? request.getDescription() : "Transfer to " + maskAccount(toAccount.getAccountNumber()));
        debitTransaction.setSuccessful(true);
        transactionRepository.save(debitTransaction);

        Transaction creditTransaction = createTransaction(toAccount, TransactionType.TRANSFER,
                request.getAmount(), toBalanceBefore, toAccount.getBalance(),
                fromAccount.getAccountNumber(),
                request.getDescription() != null ? request.getDescription() : "Transfer from " + maskAccount(fromAccount.getAccountNumber()));
        creditTransaction.setSuccessful(true);
        transactionRepository.save(creditTransaction);

        auditService.logEvent("TRANSFER_SUCCESS", fromAccount.getAccountNumber(),
                fromAccount.getAccountHolderName(),
                String.format("Transfer of %.2f to %s successful",
                        request.getAmount(), maskAccount(toAccount.getAccountNumber())),
                true, null, httpRequest);

        log.info("ATMService: Transfer successful from: {} to: {}, amount: {}",
                maskAccount(request.getFromAccount()),
                maskAccount(request.getToAccount()),
                request.getAmount());

        return buildTransactionResponse(debitTransaction);
    }

    @Transactional
    public BalanceResponse getBalance(String accountNumber, HttpServletRequest httpRequest) {
        log.info("ATMService: Balance inquiry for account: {}", maskAccount(accountNumber));

        Account account = accountRepository.findByAccountNumberWithLock(accountNumber)
                .orElseThrow(() -> new InvalidPinException("Account not found"));

        validateAccountAccess(account);

        auditService.logEvent("BALANCE_INQUIRY", account.getAccountNumber(),
                account.getAccountHolderName(),
                "Balance inquiry", true, null, httpRequest);

        return BalanceResponse.builder()
                .accountNumber(account.getAccountNumber())
                .accountHolderName(account.getAccountHolderName())
                .balance(account.getBalance())
                .accountType(account.getAccountType())
                .success(true)
                .message("Balance retrieved successfully")
                .build();
    }

    public List<TransactionResponse> getMiniStatement(String accountNumber, HttpServletRequest httpRequest) {
        log.info("ATMService: Mini statement for account: {}", maskAccount(accountNumber));

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new InvalidPinException("Account not found"));

        validateAccountAccess(account);

        List<Transaction> transactions = transactionRepository
                .findRecentTransactions(account, PageRequest.of(0, AppConstants.MINI_STATEMENT_LIMIT));

        auditService.logEvent("MINI_STATEMENT", account.getAccountNumber(),
                account.getAccountHolderName(),
                "Mini statement viewed", true, null, httpRequest);

        return transactions.stream()
                .map(this::buildTransactionResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public LoginResponse changePin(PinChangeRequest request, HttpServletRequest httpRequest) {
        log.info("ATMService: PIN change attempt for account: {}", maskAccount(request.getAccountNumber()));

        Account account = accountRepository.findByAccountNumberWithLock(request.getAccountNumber())
                .orElseThrow(() -> new InvalidPinException("Account not found"));

        validateAccountAccess(account);

        if (!account.getPin().equals(request.getOldPin())) {
            auditService.logEvent("PIN_CHANGE_FAILED", account.getAccountNumber(),
                    account.getAccountHolderName(),
                    "Invalid old PIN", false, "Invalid old PIN", httpRequest);
            throw new InvalidPinException("Invalid old PIN");
        }

        if (request.getNewPin().length() < AppConstants.PIN_MIN_LENGTH ||
            request.getNewPin().length() > AppConstants.PIN_MAX_LENGTH) {
            throw new InvalidPinException(
                    String.format("New PIN must be between %d and %d digits",
                            AppConstants.PIN_MIN_LENGTH, AppConstants.PIN_MAX_LENGTH));
        }

        if (!request.getNewPin().matches("\\d+")) {
            throw new InvalidPinException("New PIN must contain only digits");
        }

        if (request.getOldPin().equals(request.getNewPin())) {
            throw new InvalidPinException("New PIN must be different from old PIN");
        }

        account.setPin(request.getNewPin());
        accountRepository.save(account);

        Transaction transaction = createTransaction(account, TransactionType.PIN_CHANGE,
                BigDecimal.ZERO, account.getBalance(), account.getBalance(), null, "PIN changed successfully");
        transaction.setSuccessful(true);
        transactionRepository.save(transaction);

        auditService.logEvent("PIN_CHANGE_SUCCESS", account.getAccountNumber(),
                account.getAccountHolderName(),
                "PIN changed successfully", true, null, httpRequest);

        log.info("ATMService: PIN change successful for account: {}", maskAccount(request.getAccountNumber()));

        return LoginResponse.builder()
                .success(true)
                .message("PIN changed successfully")
                .accountNumber(account.getAccountNumber())
                .accountHolderName(account.getAccountHolderName())
                .build();
    }

    @Transactional
    public LoginResponse logout(String accountNumber, HttpServletRequest httpRequest) {
        log.info("ATMService: Logout for account: {}", maskAccount(accountNumber));

        try {
            atmHardware.ejectCard();
        } catch (Exception e) {
            log.warn("ATMService: Card ejection failed during logout: {}", e.getMessage());
        }

        auditService.logEvent("LOGOUT", accountNumber, null,
                "User logged out", true, null, httpRequest);

        log.info("ATMService: Logout successful for account: {}", maskAccount(accountNumber));

        return LoginResponse.builder()
                .success(true)
                .message("Logout successful. Card ejected.")
                .build();
    }

    public ATMStatusResponse getATMStatus() {
        return ATMStatusResponse.builder()
                .online(atmHardware.isOnline())
                .availableCash(atmHardware.getAvailableCash())
                .availableNotes(atmHardware.getAvailableNotes())
                .paperLevel(atmHardware.getPaperLevel())
                .lowOnCash(atmHardware.isLowOnCash())
                .lastMaintenance(atmHardware.getLastMaintenance())
                .build();
    }

    private void validateAccountAccess(Account account) {
        if (!account.isActive()) {
            throw new AccountLockedException("Account is deactivated. Please contact the bank.");
        }

        if (account.isLocked()) {
            if (account.getLockTime() != null &&
                account.getLockTime().plusMinutes(AppConstants.ACCOUNT_LOCK_DURATION_MINUTES).isBefore(LocalDateTime.now())) {
                account.setLocked(false);
                account.setFailedAttempts(0);
                account.setLockTime(null);
                accountRepository.save(account);
                return;
            }
            throw new AccountLockedException(
                String.format("Account is locked due to multiple failed PIN attempts. " +
                        "Please try again after %d minutes.", AppConstants.ACCOUNT_LOCK_DURATION_MINUTES));
        }
    }

    private void validatePinMatch(Account account, String pin) {
        if (!account.getPin().equals(pin)) {
            throw new InvalidPinException("Invalid PIN for this transaction");
        }
    }

    private void handleFailedLogin(Account account, HttpServletRequest httpRequest) {
        account.setFailedAttempts(account.getFailedAttempts() + 1);
        int remainingAttempts = AppConstants.MAX_PIN_ATTEMPTS - account.getFailedAttempts();

        String message;
        if (account.getFailedAttempts() >= AppConstants.MAX_PIN_ATTEMPTS) {
            account.setLocked(true);
            account.setLockTime(LocalDateTime.now());
            accountRepository.save(account);

            auditService.logEvent("ACCOUNT_LOCKED", account.getAccountNumber(),
                    account.getAccountHolderName(),
                    "Account locked due to maximum PIN attempts exceeded",
                    false, "Account locked", httpRequest);

            throw new AccountLockedException(
                    "Account has been locked due to multiple failed PIN attempts. " +
                    "Please try again after " + AppConstants.ACCOUNT_LOCK_DURATION_MINUTES + " minutes.");
        }

        accountRepository.save(account);

        auditService.logEvent("LOGIN_FAILED", account.getAccountNumber(),
                account.getAccountHolderName(),
                String.format("Invalid PIN. Attempts remaining: %d", remainingAttempts),
                false, "Invalid PIN", httpRequest);

        throw new InvalidPinException(
                String.format("Invalid PIN. %d attempt(s) remaining.", remainingAttempts),
                remainingAttempts);
    }

    private void checkDailyLimit(Account account, BigDecimal amount) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = LocalDateTime.of(today, LocalTime.MIDNIGHT);

        if (account.getLastWithdrawalDate() == null ||
            !account.getLastWithdrawalDate().toLocalDate().equals(today)) {
            account.setDailyWithdrawalTotal(BigDecimal.ZERO);
            account.setLastWithdrawalDate(LocalDateTime.now());
        }

        BigDecimal dailyTotal = transactionRepository.getDailyWithdrawalTotal(account, startOfDay);
        BigDecimal newTotal = dailyTotal.add(amount);

        if (newTotal.compareTo(BigDecimal.valueOf(AppConstants.MAX_DAILY_WITHDRAWAL_LIMIT)) > 0) {
            throw new DailyLimitExceededException(
                    String.format("Daily withdrawal limit of %.2f exceeded. " +
                            "Current daily total: %.2f, Requested: %.2f",
                            AppConstants.MAX_DAILY_WITHDRAWAL_LIMIT, dailyTotal, amount),
                    AppConstants.MAX_DAILY_WITHDRAWAL_LIMIT,
                    dailyTotal.doubleValue()
            );
        }
    }

    private void updateDailyWithdrawalTotal(Account account, BigDecimal amount) {
        if (account.getDailyWithdrawalTotal() == null) {
            account.setDailyWithdrawalTotal(BigDecimal.ZERO);
        }
        account.setDailyWithdrawalTotal(account.getDailyWithdrawalTotal().add(amount));
        account.setLastWithdrawalDate(LocalDateTime.now());
    }

    private Transaction createTransaction(Account account, TransactionType type,
                                           BigDecimal amount, BigDecimal balanceBefore,
                                           BigDecimal balanceAfter, String targetAccount,
                                           String description) {
        Transaction transaction = Transaction.builder()
                .transactionId(idGenerator.generateTransactionId())
                .account(account)
                .transactionType(type)
                .amount(amount)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .targetAccount(targetAccount)
                .description(description)
                .isSuccessful(false)
                .transactionDate(LocalDateTime.now())
                .build();
        return transactionRepository.save(transaction);
    }

    private TransactionResponse buildTransactionResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .transactionId(transaction.getTransactionId())
                .transactionType(transaction.getTransactionType().name())
                .amount(transaction.getAmount())
                .balanceBefore(transaction.getBalanceBefore())
                .balanceAfter(transaction.getBalanceAfter())
                .targetAccount(transaction.getTargetAccount())
                .description(transaction.getDescription())
                .successful(transaction.isSuccessful())
                .transactionDate(transaction.getTransactionDate())
                .build();
    }

    private String maskAccount(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 4) {
            return "****";
        }
        return "****" + accountNumber.substring(accountNumber.length() - 4);
    }
}