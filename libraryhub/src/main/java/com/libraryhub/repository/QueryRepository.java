package com.libraryhub.repository;

import java.time.LocalDateTime;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.libraryhub.model.QueryStatus;
import com.libraryhub.util.DateUtil;

@Repository
public class QueryRepository {

    private final JdbcTemplate jdbc;

    public QueryRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public long insert(Long userId, String name, String email, String subject, String message,
                       QueryStatus status, String adminReply, LocalDateTime createdAt,
                       LocalDateTime resolvedAt) {
        return JdbcSupport.insert(jdbc,
                "INSERT INTO queries (user_id, name, email, subject, message, status, admin_reply, "
                        + "created_at, resolved_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                userId, name, email, subject, message, status.name(), adminReply,
                DateUtil.toDb(createdAt), DateUtil.toDb(resolvedAt));
    }
}
