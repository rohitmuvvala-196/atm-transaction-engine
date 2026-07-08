package com.atmengine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BalanceResponse {
    private String accountNumber;
    private String accountHolderName;
    private BigDecimal balance;
    private String accountType;
    private boolean success;
    private String message;
}