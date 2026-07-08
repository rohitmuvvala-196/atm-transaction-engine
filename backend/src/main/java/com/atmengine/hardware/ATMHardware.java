package com.atmengine.hardware;

import com.atmengine.exception.ATMOfflineException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Random;

@Component
@Slf4j
public class ATMHardware {

    private final CardReader cardReader;
    private final CashDispenser cashDispenser;
    private final ReceiptPrinter receiptPrinter;
    private final Random random = new Random();

    @Getter
    private boolean online = true;

    @Getter
    private LocalDateTime lastMaintenance;

    public ATMHardware(CardReader cardReader, CashDispenser cashDispenser, ReceiptPrinter receiptPrinter) {
        this.cardReader = cardReader;
        this.cashDispenser = cashDispenser;
        this.receiptPrinter = receiptPrinter;
        this.lastMaintenance = LocalDateTime.now();
    }

    public boolean insertCard(String cardNumber) {
        checkOnline();
        return cardReader.insertCard(cardNumber);
    }

    public boolean ejectCard() {
        checkOnline();
        return cardReader.ejectCard();
    }

    public boolean dispenseCash(double amount) {
        checkOnline();
        return cashDispenser.dispenseCash(amount);
    }

    public boolean printReceipt(String receiptContent) {
        checkOnline();
        return receiptPrinter.printReceipt(receiptContent);
    }

    public String generateReceipt(String transactionId, String transactionType, double amount,
                                   double balanceBefore, double balanceAfter, String accountNumber) {
        return receiptPrinter.generateReceipt(transactionId, transactionType, amount,
                balanceBefore, balanceAfter, accountNumber);
    }

    public double getAvailableCash() {
        return cashDispenser.getAvailableCash();
    }

    public int getAvailableNotes() {
        return cashDispenser.getAvailableNotes();
    }

    public boolean isLowOnCash() {
        return cashDispenser.isLowOnCash();
    }

    public int getPaperLevel() {
        return receiptPrinter.getPaperLevel();
    }

    public boolean isCardInserted() {
        return cardReader.isCardInserted();
    }

    public void performMaintenance() {
        log.info("ATMHardware: Performing scheduled maintenance");
        this.online = true;
        this.lastMaintenance = LocalDateTime.now();
        cashDispenser.reloadCash(500);
        receiptPrinter.reloadPaper();
        log.info("ATMHardware: Maintenance completed successfully");
    }

    public void setOffline() {
        this.online = false;
        log.warn("ATMHardware: ATM set to offline mode");
    }

    public void setOnline() {
        this.online = true;
        log.info("ATMHardware: ATM set to online mode");
    }

    private void checkOnline() {
        if (!online) {
            throw new ATMOfflineException("ATM is currently offline. Please try again later.");
        }
        if (random.nextDouble() < 0.01) {
            log.warn("ATMHardware: Random connectivity issue detected");
            throw new ATMOfflineException("ATM is experiencing connectivity issues. Please try again.");
        }
    }
}