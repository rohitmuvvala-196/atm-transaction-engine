package com.atmengine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ATMStatusResponse {
    private boolean online;
    private double availableCash;
    private int availableNotes;
    private int paperLevel;
    private boolean lowOnCash;
    private LocalDateTime lastMaintenance;
}