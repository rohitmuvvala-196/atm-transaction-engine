package com.atmengine.exception;

public class TransactionRollbackException extends RuntimeException {
    private final String transactionId;
    private final double rolledBackAmount;

    public TransactionRollbackException(String message, String transactionId, double rolledBackAmount) {
        super(message);
        this.transactionId = transactionId;
        this.rolledBackAmount = rolledBackAmount;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public double getRolledBackAmount() {
        return rolledBackAmount;
    }
}