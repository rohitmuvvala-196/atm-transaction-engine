package com.atmengine.hardware;

import com.atmengine.exception.HardwareFailureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Component
@Slf4j
public class ReceiptPrinter {

    private static final double FAILURE_PROBABILITY = 0.03;
    private final Random random = new Random();
    private int paperLevel = 100;

    public boolean printReceipt(String receiptContent) {
        log.info("ReceiptPrinter: Attempting to print receipt");

        if (simulateFailure()) {
            log.error("ReceiptPrinter: Hardware failure detected");
            throw new HardwareFailureException(
                "Receipt printer hardware failure: Unable to print receipt. " +
                "Please check the printer and try again.",
                "ReceiptPrinter"
            );
        }

        if (paperLevel <= 0) {
            log.error("ReceiptPrinter: Out of paper");
            throw new HardwareFailureException(
                "Receipt printer is out of paper. Please contact the bank.",
                "ReceiptPrinter"
            );
        }

        paperLevel -= 5;
        log.info("ReceiptPrinter: Receipt printed successfully. Paper remaining: {}%", paperLevel);
        return true;
    }

    public String generateReceipt(String transactionId, String transactionType, double amount,
                                   double balanceBefore, double balanceAfter, String accountNumber) {
        StringBuilder receipt = new StringBuilder();
        receipt.append("================================\n");
        receipt.append("         ATM TRANSACTION         \n");
        receipt.append("================================\n");
        receipt.append("Date: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        receipt.append("--------------------------------\n");
        receipt.append("Transaction ID: ").append(transactionId).append("\n");
        receipt.append("Transaction Type: ").append(transactionType).append("\n");
        receipt.append("Account: ****").append(accountNumber.substring(Math.max(0, accountNumber.length() - 4))).append("\n");
        receipt.append("--------------------------------\n");
        receipt.append("Amount: ").append(String.format("%.2f", amount)).append("\n");
        receipt.append("Balance Before: ").append(String.format("%.2f", balanceBefore)).append("\n");
        receipt.append("Balance After: ").append(String.format("%.2f", balanceAfter)).append("\n");
        receipt.append("--------------------------------\n");
        receipt.append("Thank you for using our ATM!\n");
        receipt.append("================================\n");
        return receipt.toString();
    }

    public void reloadPaper() {
        paperLevel = 100;
        log.info("ReceiptPrinter: Paper reloaded to 100%");
    }

    public int getPaperLevel() {
        return paperLevel;
    }

    private boolean simulateFailure() {
        return random.nextDouble() < FAILURE_PROBABILITY;
    }
}