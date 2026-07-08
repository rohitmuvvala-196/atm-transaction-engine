package com.atmengine.validation;

import com.atmengine.constants.AppConstants;
import com.atmengine.exception.InvalidAmountException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Slf4j
public class ATMValidator {

    public void validateWithdrawalAmount(BigDecimal amount) {
        if (amount == null) {
            throw new InvalidAmountException("Withdrawal amount cannot be null", 0);
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Withdrawal amount must be greater than zero", amount.doubleValue());
        }

        if (amount.compareTo(BigDecimal.valueOf(AppConstants.MIN_WITHDRAWAL_AMOUNT)) < 0) {
            throw new InvalidAmountException(
                String.format("Minimum withdrawal amount is %.2f", AppConstants.MIN_WITHDRAWAL_AMOUNT),
                amount.doubleValue()
            );
        }

        if (amount.compareTo(BigDecimal.valueOf(AppConstants.MAX_DAILY_WITHDRAWAL_LIMIT)) > 0) {
            throw new InvalidAmountException(
                String.format("Maximum withdrawal amount per transaction is %.2f", AppConstants.MAX_DAILY_WITHDRAWAL_LIMIT),
                amount.doubleValue()
            );
        }

        // Validate amount is multiple of 100
        if (amount.remainder(BigDecimal.valueOf(100)).compareTo(BigDecimal.ZERO) != 0) {
            throw new InvalidAmountException("Withdrawal amount must be a multiple of 100", amount.doubleValue());
        }
    }

    public void validateDepositAmount(BigDecimal amount) {
        if (amount == null) {
            throw new InvalidAmountException("Deposit amount cannot be null", 0);
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Deposit amount must be greater than zero", amount.doubleValue());
        }

        if (amount.compareTo(BigDecimal.valueOf(AppConstants.MIN_DEPOSIT_AMOUNT)) < 0) {
            throw new InvalidAmountException(
                String.format("Minimum deposit amount is %.2f", AppConstants.MIN_DEPOSIT_AMOUNT),
                amount.doubleValue()
            );
        }

        if (amount.compareTo(BigDecimal.valueOf(AppConstants.MAX_DEPOSIT_AMOUNT)) > 0) {
            throw new InvalidAmountException(
                String.format("Maximum deposit amount is %.2f", AppConstants.MAX_DEPOSIT_AMOUNT),
                amount.doubleValue()
            );
        }
    }

    public void validateTransferAmount(BigDecimal amount) {
        if (amount == null) {
            throw new InvalidAmountException("Transfer amount cannot be null", 0);
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Transfer amount must be greater than zero", amount.doubleValue());
        }

        if (amount.compareTo(BigDecimal.valueOf(AppConstants.MAX_TRANSACTION_LIMIT)) > 0) {
            throw new InvalidAmountException(
                String.format("Maximum transfer amount per transaction is %.2f", AppConstants.MAX_TRANSACTION_LIMIT),
                amount.doubleValue()
            );
        }
    }

    public void validatePin(String pin) {
        if (pin == null || pin.trim().isEmpty()) {
            throw new InvalidAmountException("PIN cannot be empty", 0);
        }
        if (pin.length() < AppConstants.PIN_MIN_LENGTH || pin.length() > AppConstants.PIN_MAX_LENGTH) {
            throw new InvalidAmountException(
                String.format("PIN must be between %d and %d digits", AppConstants.PIN_MIN_LENGTH, AppConstants.PIN_MAX_LENGTH),
                0
            );
        }
        if (!pin.matches("\\d+")) {
            throw new InvalidAmountException("PIN must contain only digits", 0);
        }
    }
}