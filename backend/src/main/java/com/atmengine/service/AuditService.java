package com.atmengine.service;

import com.atmengine.entity.AuditLog;
import com.atmengine.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public void logEvent(String eventType, String accountNumber, String username,
                         String actionDetail, boolean success, String failureReason,
                         HttpServletRequest request) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .eventType(eventType)
                    .accountNumber(accountNumber)
                    .username(username)
                    .actionDetail(actionDetail)
                    .ipAddress(request != null ? request.getRemoteAddr() : "UNKNOWN")
                    .userAgent(request != null ? request.getHeader("User-Agent") : "UNKNOWN")
                    .isSuccessful(success)
                    .failureReason(failureReason)
                    .build();

            auditLogRepository.save(auditLog);
            log.info("Audit event logged: {} - {} - {}", eventType, accountNumber, success ? "SUCCESS" : "FAILED");
        } catch (Exception e) {
            log.error("Failed to save audit log: {}", e.getMessage());
        }
    }

    public List<AuditLog> getAccountAuditLogs(String accountNumber) {
        return auditLogRepository.findByAccountNumberOrderByCreatedAtDesc(accountNumber);
    }

    public List<AuditLog> getAuditLogsByDateRange(LocalDateTime start, LocalDateTime end) {
        return auditLogRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(start, end);
    }
}