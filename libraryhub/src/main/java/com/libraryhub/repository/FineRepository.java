package com.libraryhub.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.libraryhub.util.DateUtil;

@Repository
public class FineRepository {

    private final JdbcTemplate jdbc;

    public FineRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public long insert(long issueId, long userId, int daysOverdue, BigDecimal amount, boolean paid,
                       LocalDateTime createdAt, LocalDateTime paidAt, Long paidBy) {
        return JdbcSupport.insert(jdbc,
                "INSERT INTO fines (issue_id, user_id, days_overdue, amount, paid, created_at, paid_at, paid_by) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                issueId, userId, daysOverdue, JdbcSupport.toDb(amount), paid ? 1 : 0,
                DateUtil.toDb(createdAt), DateUtil.toDb(paidAt), paidBy);
    }

    public BigDecimal sumUnpaidByUser(long userId) {
        return JdbcSupport.money(jdbc,
                "SELECT COALESCE(ROUND(SUM(amount), 2), 0) FROM fines WHERE user_id = ? AND paid = 0", userId);
    }
}
