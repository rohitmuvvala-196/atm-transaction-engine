package com.atmengine.constants;

public final class AppConstants {

    private AppConstants() {}

    public static final int MAX_PIN_ATTEMPTS = 3;
    public static final int ACCOUNT_LOCK_DURATION_MINUTES = 30;
    public static final double MAX_DAILY_WITHDRAWAL_LIMIT = 100000.0;
    public static final double MAX_TRANSACTION_LIMIT = 50000.0;
    public static final double MIN_DEPOSIT_AMOUNT = 100.0;
    public static final double MIN_WITHDRAWAL_AMOUNT = 100.0;
    public static final double MAX_DEPOSIT_AMOUNT = 500000.0;
    public static final int MINI_STATEMENT_LIMIT = 20;
    public static final int PIN_MIN_LENGTH = 4;
    public static final int PIN_MAX_LENGTH = 6;
    public static final String DEFAULT_CURRENCY = "INR";
    public static final String API_BASE_PATH = "/api/v1";
    public static final String SYSTEM_ACCOUNT = "SYSTEM";
    public static final String ATM_CASH_ACCOUNT = "ATM_CASH";
    public static final double INITIAL_ATM_CASH_BALANCE = 5000000.0;
    public static final double LOW_ATM_CASH_THRESHOLD = 500000.0;
}