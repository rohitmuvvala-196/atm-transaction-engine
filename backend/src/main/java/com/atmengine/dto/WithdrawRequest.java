package com.atmengine.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WithdrawRequest {

    @NotBlank(message = "Account number is required")
    private String accountNumber;

    @NotBlank(message = "PIN is required")
    @Size(min = 4, max = 6, message = "PIN must be between 4 and 6 digits")
    private String pin;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "100.00", message = "Minimum withdrawal amount is 100")
    private BigDecimal amount;
}