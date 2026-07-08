package com.atmengine.controller;

import com.atmengine.dto.*;
import com.atmengine.response.ApiResponse;
import com.atmengine.service.ATMService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/atm")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ATMController {

    private final ATMService atmService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        log.info("REST: Login request received");
        LoginResponse response = atmService.login(request, httpRequest);
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<ApiResponse<TransactionResponse>> withdraw(
            @Valid @RequestBody WithdrawRequest request,
            HttpServletRequest httpRequest) {
        log.info("REST: Withdrawal request received for amount: {}", request.getAmount());
        TransactionResponse response = atmService.withdraw(request, httpRequest);
        return ResponseEntity.ok(ApiResponse.success(response, "Withdrawal successful"));
    }

    @PostMapping("/deposit")
    public ResponseEntity<ApiResponse<TransactionResponse>> deposit(
            @Valid @RequestBody DepositRequest request,
            HttpServletRequest httpRequest) {
        log.info("REST: Deposit request received for amount: {}", request.getAmount());
        TransactionResponse response = atmService.deposit(request, httpRequest);
        return ResponseEntity.ok(ApiResponse.success(response, "Deposit successful"));
    }

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<TransactionResponse>> transfer(
            @Valid @RequestBody TransferRequest request,
            HttpServletRequest httpRequest) {
        log.info("REST: Transfer request received");
        TransactionResponse response = atmService.transfer(request, httpRequest);
        return ResponseEntity.ok(ApiResponse.success(response, "Transfer successful"));
    }

    @GetMapping("/balance")
    public ResponseEntity<ApiResponse<BalanceResponse>> getBalance(
            @RequestParam String accountNumber,
            HttpServletRequest httpRequest) {
        log.info("REST: Balance inquiry for account: {}", maskAccount(accountNumber));
        BalanceResponse response = atmService.getBalance(accountNumber, httpRequest);
        return ResponseEntity.ok(ApiResponse.success(response, "Balance retrieved successfully"));
    }

    @GetMapping("/statement")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getStatement(
            @RequestParam String accountNumber,
            HttpServletRequest httpRequest) {
        log.info("REST: Mini statement request for account: {}", maskAccount(accountNumber));
        List<TransactionResponse> response = atmService.getMiniStatement(accountNumber, httpRequest);
        return ResponseEntity.ok(ApiResponse.success(response, "Statement retrieved successfully"));
    }

    @PutMapping("/change-pin")
    public ResponseEntity<ApiResponse<LoginResponse>> changePin(
            @Valid @RequestBody PinChangeRequest request,
            HttpServletRequest httpRequest) {
        log.info("REST: PIN change request received");
        LoginResponse response = atmService.changePin(request, httpRequest);
        return ResponseEntity.ok(ApiResponse.success(response, "PIN changed successfully"));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<LoginResponse>> logout(
            @RequestParam String accountNumber,
            HttpServletRequest httpRequest) {
        log.info("REST: Logout request for account: {}", maskAccount(accountNumber));
        LoginResponse response = atmService.logout(accountNumber, httpRequest);
        return ResponseEntity.ok(ApiResponse.success(response, "Logout successful"));
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<ATMStatusResponse>> getATMStatus() {
        log.info("REST: ATM status request");
        ATMStatusResponse status = atmService.getATMStatus();
        return ResponseEntity.ok(ApiResponse.success(status, "ATM status retrieved"));
    }

    private String maskAccount(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 4) {
            return "****";
        }
        return "****" + accountNumber.substring(accountNumber.length() - 4);
    }
}