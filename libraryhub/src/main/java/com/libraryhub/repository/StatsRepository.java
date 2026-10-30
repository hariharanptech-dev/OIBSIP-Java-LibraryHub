package com.libraryhub.repository;

import java.time.LocalDate;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.libraryhub.model.DashboardStats;
import com.libraryhub.util.DateUtil;

@Repository
public class StatsRepository {

    private final JdbcTemplate jdbc;

    public StatsRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public long titleCount() {
        return JdbcSupport.count(jdbc, "SELECT COUNT(*) FROM books");
    }

    public long copyCount() {
        return JdbcSupport.count(jdbc, "SELECT COALESCE(SUM(total_quantity), 0) FROM books");
    }

    public long memberCount() {
        return JdbcSupport.count(jdbc, "SELECT COUNT(*) FROM users WHERE role = 'USER'");
    }

    /** Headline numbers for the admin dashboard. A loan is overdue when its due date is before today. */
    public DashboardStats dashboardStats(LocalDate today) {
        return new DashboardStats(
                titleCount(),
                copyCount(),
                JdbcSupport.count(jdbc, "SELECT COUNT(*) FROM issues WHERE status = 'ISSUED'"),
                JdbcSupport.count(jdbc,
                        "SELECT COUNT(*) FROM issues WHERE status = 'ISSUED' AND due_date < ?",
                        DateUtil.toDb(today)),
                memberCount(),
                JdbcSupport.money(jdbc,
                        "SELECT COALESCE(ROUND(SUM(amount), 2), 0) FROM fines WHERE paid = 0"),
                JdbcSupport.count(jdbc, "SELECT COUNT(*) FROM queries WHERE status = 'NEW'"));
    }
}
