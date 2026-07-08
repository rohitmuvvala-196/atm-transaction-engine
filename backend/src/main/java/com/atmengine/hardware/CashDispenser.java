package com.atmengine.hardware;

import com.atmengine.exception.CashDispenseException;
import com.atmengine.exception.HardwareFailureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
public class CashDispenser {

    private static final double FAILURE_PROBABILITY = 0.05;
    private static final int MAX_CAPACITY = 1000;
    private final Random random = new Random();
    private final AtomicInteger availableNotes = new AtomicInteger(MAX_CAPACITY);

    public boolean dispenseCash(double amount) {
        log.info("CashDispenser: Attempting to dispense amount: {}", amount);

        if (simulateFailure()) {
            log.error("CashDispenser: Hardware failure detected during cash dispensing");
            throw new CashDispenseException(
                "Cash dispenser hardware failure: Unable to dispense cash. " +
                "Your account will not be debited. Please try again.",
                amount,
                "Cash dispenser jammed"
            );
        }

        int notesRequired = calculateNotesRequired(amount);
        if (notesRequired > availableNotes.get()) {
            log.error("CashDispenser: Insufficient notes available. Required: {}, Available: {}",
                      notesRequired, availableNotes.get());
            throw new CashDispenseException(
                "Cash dispenser has insufficient notes. Please contact the bank.",
                amount,
                "Insufficient notes in dispenser"
            );
        }

        availableNotes.addAndGet(-notesRequired);
        log.info("CashDispenser: Successfully dispensed {}. Remaining notes: {}", amount, availableNotes.get());
        return true;
    }

    public int getAvailableNotes() {
        return availableNotes.get();
    }

    public double getAvailableCash() {
        return availableNotes.get() * 100.0;
    }

    public void reloadCash(int notes) {
        int newTotal = Math.min(availableNotes.addAndGet(notes), MAX_CAPACITY);
        log.info("CashDispenser: Reloaded with {} notes. Total available: {}", notes, newTotal);
    }

    public boolean isLowOnCash() {
        return availableNotes.get() < 100;
    }

    private int calculateNotesRequired(double amount) {
        return (int) Math.ceil(amount / 100.0);
    }

    private boolean simulateFailure() {
        return random.nextDouble() < FAILURE_PROBABILITY;
    }
}