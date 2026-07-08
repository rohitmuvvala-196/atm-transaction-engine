package com.atmengine.exception;

public class DailyLimitExceededException extends RuntimeException {
    private final double dailyLimit;
    private final double currentDailyTotal;

    public DailyLimitExceededException(String message, double dailyLimit, double currentDailyTotal) {
        super(message);
        this.dailyLimit = dailyLimit;
        this.currentDailyTotal = currentDailyTotal;
    }

    public double getDailyLimit() {
        return dailyLimit;
    }

    public double getCurrentDailyTotal() {
        return currentDailyTotal;
    }
}