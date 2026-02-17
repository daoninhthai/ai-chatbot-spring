package com.daoninhthai.aichatbot.repository;

import com.daoninhthai.aichatbot.entity.UsageRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface UsageRecordRepository extends JpaRepository<UsageRecord, Long> {

    List<UsageRecord> findByUserIdAndTimestampBetween(Long userId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT SUM(u.totalTokens) FROM UsageRecord u WHERE u.userId = :userId AND u.timestamp >= :since")
    Long sumTotalTokensByUserSince(Long userId, LocalDateTime since);

    @Query("SELECT COUNT(u) FROM UsageRecord u WHERE u.userId = :userId AND u.timestamp >= :since")
    long countByUserIdSince(Long userId, LocalDateTime since);

    @Query("SELECT SUM(u.cost) FROM UsageRecord u WHERE u.userId = :userId AND u.timestamp BETWEEN :start AND :end")
    java.math.BigDecimal sumCostByUserAndPeriod(Long userId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT u.model, COUNT(u), SUM(u.totalTokens) FROM UsageRecord u WHERE u.userId = :userId GROUP BY u.model")
    List<Object[]> getUsageByModel(Long userId);

    @Query("SELECT SUM(u.totalTokens) FROM UsageRecord u WHERE u.timestamp >= :since")
    Long sumAllTokensSince(LocalDateTime since);

    long countByTimestampAfter(LocalDateTime since);
}
