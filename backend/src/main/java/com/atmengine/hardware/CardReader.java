package com.atmengine.hardware;

import com.atmengine.exception.HardwareFailureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
@Slf4j
public class CardReader {

    private static final double FAILURE_PROBABILITY = 0.02;
    private final Random random = new Random();
    private boolean cardInserted = false;

    public boolean insertCard(String cardNumber) {
        log.info("CardReader: Attempting to insert card: {}", maskCardNumber(cardNumber));

        if (simulateFailure()) {
            log.error("CardReader: Hardware failure detected during card insertion");
            throw new HardwareFailureException(
                "Card reader hardware failure: Unable to read card. Please try again.",
                "CardReader"
            );
        }

        cardInserted = true;
        log.info("CardReader: Card {} inserted successfully", maskCardNumber(cardNumber));
        return true;
    }

    public boolean ejectCard() {
        log.info("CardReader: Ejecting card");

        if (!cardInserted) {
            log.warn("CardReader: No card to eject");
            return true;
        }

        if (simulateFailure()) {
            log.error("CardReader: Hardware failure during card ejection");
            throw new HardwareFailureException(
                "Card reader hardware failure: Unable to eject card. Manual intervention required.",
                "CardReader"
            );
        }

        cardInserted = false;
        log.info("CardReader: Card ejected successfully");
        return true;
    }

    public boolean isCardInserted() {
        return cardInserted;
    }

    public String readCardData() {
        if (!cardInserted) {
            throw new HardwareFailureException("No card inserted in the reader", "CardReader");
        }

        if (simulateFailure()) {
            log.error("CardReader: Failed to read card data");
            throw new HardwareFailureException(
                "Card reader error: Unable to read card data. Please clean the card and try again.",
                "CardReader"
            );
        }

        return "CARD_DATA_READ_SUCCESSFULLY";
    }

    private boolean simulateFailure() {
        return random.nextDouble() < FAILURE_PROBABILITY;
    }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        return "****" + cardNumber.substring(cardNumber.length() - 4);
    }
}