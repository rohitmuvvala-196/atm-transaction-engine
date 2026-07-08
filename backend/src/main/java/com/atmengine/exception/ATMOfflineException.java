package com.atmengine.exception;

public class ATMOfflineException extends RuntimeException {
    public ATMOfflineException(String message) {
        super(message);
    }
}