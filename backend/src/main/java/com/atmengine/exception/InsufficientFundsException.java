package com.atmengine.exception;

public class InsufficientFundsException extends RuntimeException {
    private final double availableBalance;
    private final double requestedAmount;

    public InsufficientFundsException(String message, double availableBalance, double requestedAmount) {
        super(message);
        this.availableBalance = availableBalance;
        this.requestedAmount = requestedAmount;
    }

    public double getAvailableBalance() {
        return availableBalance;
    }

    public double getRequestedAmount() {
        return requestedAmount;
    }
}