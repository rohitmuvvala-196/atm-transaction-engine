package com.atmengine.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class TransactionIdGenerator {

    private static final AtomicLong sequence = new AtomicLong(0);
    private static final String PREFIX = "TXN";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public String generateTransactionId() {
        String timestamp = LocalDateTime.now().format(DATE_FORMAT);
        String uniqueId = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        long seq = sequence.incrementAndGet() % 10000;
        return String.format("%s-%s-%s-%04d", PREFIX, timestamp, uniqueId, seq);
    }

    public String generateAccountNumber() {
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(5);
        String randomPart = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return "ACC" + timestamp + randomPart;
    }

    public String generateCardNumber() {
        String randomPart = UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        return "4" + randomPart;
    }
}