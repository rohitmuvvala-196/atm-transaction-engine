package com.atmengine.exception;

public class CashDispenseException extends RuntimeException {
    private final double amount;
    private final String reason;

    public CashDispenseException(String message, double amount, String reason) {
        super(message);
        this.amount = amount;
        this.reason = reason;
    }

    public double getAmount() {
        return amount;
    }

    public String getReason() {
        return reason;
    }
}