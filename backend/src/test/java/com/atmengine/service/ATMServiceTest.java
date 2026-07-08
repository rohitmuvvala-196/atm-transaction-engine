package com.atmengine.service;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ATMServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private ATMHardware atmHardware;

    @Mock
    private ATMValidator validator;

    @Mock
    private TransactionIdGenerator idGenerator;

    @Mock
    private AuditService auditService;

    @Mock
    private HttpServletRequest httpRequest;

    @InjectMocks
    private ATMService atmService;

    private Account testAccount;

    @BeforeEach
    void setUp() {
        testAccount = Account.builder()
                .id(1L)
                .accountNumber("ACC1234567890")
                .pin("1234")
                .balance(new BigDecimal("50000.00"))
                .accountHolderName("John Doe")
                .accountType("SAVINGS")
                .failedAttempts(0)
                .isLocked(false)
                .isActive(true)
                .build();
    }

    @Test
    void testLoginSuccess() {
        when(accountRepository.findByAccountNumber("ACC1234567890"))
                .thenReturn(Optional.of(testAccount));
        when(idGenerator.generateTransactionId()).thenReturn("TXN-TEST-1234");

        LoginRequest request = new LoginRequest("ACC1234567890", "1234");
        LoginResponse response = atmService.login(request, httpRequest);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("ACC1234567890", response.getAccountNumber());
        assertEquals("John Doe", response.getAccountHolderName());
        assertEquals("Login successful", response.getMessage());

        verify(accountRepository).save(testAccount);
        verify(auditService).logEvent(eq("LOGIN_SUCCESS"), anyString(), anyString(), anyString(), eq(true), isNull(), any());
    }

    @Test
    void testLoginInvalidPin() {
        when(accountRepository.findByAccountNumber("ACC1234567890"))
                .thenReturn(Optional.of(testAccount));

        LoginRequest request = new LoginRequest("ACC1234567890", "0000");

        assertThrows(InvalidPinException.class, () -> {
            atmService.login(request, httpRequest);
        });

        assertEquals(1, testAccount.getFailedAttempts());
        verify(accountRepository).save(testAccount);
    }

    @Test
    void testLoginAccountLocked() {
        testAccount.setFailedAttempts(3);
        testAccount.setLocked(true);
        testAccount.setLockTime(LocalDateTime.now());

        when(accountRepository.findByAccountNumber("ACC1234567890"))
                .thenReturn(Optional.of(testAccount));

        LoginRequest request = new LoginRequest("ACC1234567890", "1234");

        assertThrows(AccountLockedException.class, () -> {
            atmService.login(request, httpRequest);
        });
    }

    @Test
    void testWithdrawSuccess() {
        when(accountRepository.findByAccountNumberWithLock("ACC1234567890"))
                .thenReturn(Optional.of(testAccount));
        when(atmHardware.getAvailableCash()).thenReturn(100000.0);
        when(atmHardware.dispenseCash(anyDouble())).thenReturn(true);
        when(atmHardware.generateReceipt(anyString(), anyString(), anyDouble(), anyDouble(), anyDouble(), anyString()))
                .thenReturn("RECEIPT");
        when(atmHardware.printReceipt(anyString())).thenReturn(true);
        when(idGenerator.generateTransactionId()).thenReturn("TXN-TEST-WITHDRAW");
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

        WithdrawRequest request = WithdrawRequest.builder()
                .accountNumber("ACC1234567890")
                .pin("1234")
                .amount(new BigDecimal("1000.00"))
                .build();

        TransactionResponse response = atmService.withdraw(request, httpRequest);

        assertNotNull(response);
        assertTrue(response.isSuccessful());
        verify(accountRepository, atLeastOnce()).save(testAccount);
        verify(transactionRepository, atLeastOnce()).save(any(Transaction.class));
    }

    @Test
    void testWithdrawInsufficientFunds() {
        when(accountRepository.findByAccountNumberWithLock("ACC1234567890"))
                .thenReturn(Optional.of(testAccount));
        when(atmHardware.getAvailableCash()).thenReturn(100000.0);

        WithdrawRequest request = WithdrawRequest.builder()
                .accountNumber("ACC1234567890")
                .pin("1234")
                .amount(new BigDecimal("500000.00"))
                .build();

        assertThrows(InsufficientFundsException.class, () -> {
            atmService.withdraw(request, httpRequest);
        });
    }

    @Test
    void testDepositSuccess() {
        when(accountRepository.findByAccountNumberWithLock("ACC0987654321"))
                .thenReturn(Optional.of(testAccount));
        when(idGenerator.generateTransactionId()).thenReturn("TXN-TEST-DEPOSIT");
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

        DepositRequest request = DepositRequest.builder()
                .accountNumber("ACC0987654321")
                .amount(new BigDecimal("5000.00"))
                .build();

        TransactionResponse response = atmService.deposit(request, httpRequest);

        assertNotNull(response);
        assertTrue(response.isSuccessful());
        assertEquals(new BigDecimal("55000.00"), testAccount.getBalance());
    }

    @Test
    void testTransferSuccess() {
        Account targetAccount = Account.builder()
                .id(2L)
                .accountNumber("ACC0987654321")
                .pin("5678")
                .balance(new BigDecimal("100000.00"))
                .accountHolderName("Jane Smith")
                .accountType("CURRENT")
                .isActive(true)
                .build();

        when(accountRepository.findByAccountNumberWithLock("ACC1234567890"))
                .thenReturn(Optional.of(testAccount));
        when(accountRepository.findByAccountNumberWithLock("ACC0987654321"))
                .thenReturn(Optional.of(targetAccount));
        when(idGenerator.generateTransactionId()).thenReturn("TXN-TEST-TRANSFER");
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

        TransferRequest request = TransferRequest.builder()
                .fromAccount("ACC1234567890")
                .toAccount("ACC0987654321")
                .pin("1234")
                .amount(new BigDecimal("10000.00"))
                .description("Test transfer")
                .build();

        TransactionResponse response = atmService.transfer(request, httpRequest);

        assertNotNull(response);
        assertTrue(response.isSuccessful());
        assertEquals(new BigDecimal("40000.00"), testAccount.getBalance());
        assertEquals(new BigDecimal("110000.00"), targetAccount.getBalance());
    }

    @Test
    void testGetBalance() {
        when(accountRepository.findByAccountNumberWithLock("ACC1234567890"))
                .thenReturn(Optional.of(testAccount));

        BalanceResponse response = atmService.getBalance("ACC1234567890", httpRequest);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(new BigDecimal("50000.00"), response.getBalance());
        assertEquals("John Doe", response.getAccountHolderName());
    }

    @Test
    void testChangePinSuccess() {
        when(accountRepository.findByAccountNumberWithLock("ACC1234567890"))
                .thenReturn(Optional.of(testAccount));
        when(idGenerator.generateTransactionId()).thenReturn("TXN-TEST-PIN");
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

        PinChangeRequest request = PinChangeRequest.builder()
                .accountNumber("ACC1234567890")
                .oldPin("1234")
                .newPin("4321")
                .build();

        LoginResponse response = atmService.changePin(request, httpRequest);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("4321", testAccount.getPin());
        assertEquals("PIN changed successfully", response.getMessage());
    }

    @Test
    void testLogout() {
        LoginResponse response = atmService.logout("ACC1234567890", httpRequest);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Logout successful. Card ejected.", response.getMessage());
        verify(atmHardware).ejectCard();
    }

    @Test
    void testAccountLockedAfterThreeAttempts() {
        when(accountRepository.findByAccountNumber("ACC1234567890"))
                .thenReturn(Optional.of(testAccount));

        LoginRequest request = new LoginRequest("ACC1234567890", "0000");

        // First failed attempt
        assertThrows(InvalidPinException.class, () -> atmService.login(request, httpRequest));
        assertEquals(1, testAccount.getFailedAttempts());

        // Second failed attempt
        assertThrows(InvalidPinException.class, () -> atmService.login(request, httpRequest));
        assertEquals(2, testAccount.getFailedAttempts());

        // Third failed attempt - should lock
        assertThrows(AccountLockedException.class, () -> atmService.login(request, httpRequest));
        assertTrue(testAccount.isLocked());
        assertNotNull(testAccount.getLockTime());
    }
}