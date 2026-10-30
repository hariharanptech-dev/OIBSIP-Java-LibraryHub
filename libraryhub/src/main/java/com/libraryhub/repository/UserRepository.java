package com.libraryhub.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.libraryhub.model.Role;
import com.libraryhub.model.User;
import com.libraryhub.util.DateUtil;

@Repository
public class UserRepository {

    private static final String COLUMNS = "id, name, email, password_hash, role, active, created_at";

    private static final RowMapper<User> MAPPER = (rs, rowNum) -> new User(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("email"),
            rs.getString("password_hash"),
            Role.valueOf(rs.getString("role")),
            rs.getInt("active") == 1,
            DateUtil.parseDateTime(rs.getString("created_at")));

    private final JdbcTemplate jdbc;

    public UserRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<User> findById(long id) {
        return jdbc.query("SELECT " + COLUMNS + " FROM users WHERE id = ?", MAPPER, id)
                .stream().findFirst();
    }

    public Optional<User> findByEmail(String email) {
        return jdbc.query("SELECT " + COLUMNS + " FROM users WHERE email = ?", MAPPER, email)
                .stream().findFirst();
    }

    public boolean existsByEmail(String email) {
        return JdbcSupport.count(jdbc, "SELECT COUNT(*) FROM users WHERE email = ?", email) > 0;
    }

    public long count() {
        return JdbcSupport.count(jdbc, "SELECT COUNT(*) FROM users");
    }

    public long insert(String name, String email, String passwordHash, Role role, boolean active,
                       LocalDateTime createdAt) {
        return JdbcSupport.insert(jdbc,
                "INSERT INTO users (name, email, password_hash, role, active, created_at) VALUES (?, ?, ?, ?, ?, ?)",
                name, email, passwordHash, role.name(), active ? 1 : 0, DateUtil.toDb(createdAt));
    }
}
