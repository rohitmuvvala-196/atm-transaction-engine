package com.atmengine.exception;

public class HardwareFailureException extends RuntimeException {
    private final String hardwareComponent;

    public HardwareFailureException(String message, String hardwareComponent) {
        super(message);
        this.hardwareComponent = hardwareComponent;
    }

    public String getHardwareComponent() {
        return hardwareComponent;
    }
}