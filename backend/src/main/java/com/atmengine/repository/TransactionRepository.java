package com.atmengine.repository;

import com.atmengine.entity.Account;
import com.atmengine.entity.Transaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByAccountOrderByTransactionDateDesc(Account account, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.account = :account ORDER BY t.transactionDate DESC")
    List<Transaction> findRecentTransactions(@Param("account") Account account, Pageable pageable);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.account = :account " +
           "AND t.transactionType = 'WITHDRAWAL' AND t.isSuccessful = true " +
           "AND t.transactionDate >= :startOfDay")
    BigDecimal getDailyWithdrawalTotal(@Param("account") Account account,
                                       @Param("startOfDay") LocalDateTime startOfDay);

    @Query("SELECT t FROM Transaction t WHERE t.account.accountNumber = :accountNumber " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate ORDER BY t.transactionDate DESC")
    List<Transaction> findByDateRange(@Param("accountNumber") String accountNumber,
                                      @Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate);

    long countByAccountAndIsSuccessfulTrue(Account account);
}