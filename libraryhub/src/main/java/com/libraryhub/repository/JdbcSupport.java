package com.libraryhub.repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.Statement;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

/** Small helpers shared by the repositories. */
final class JdbcSupport {

    private JdbcSupport() {
    }

    /** Runs a parameterized INSERT and returns the generated row id. */
    static long insert(JdbcTemplate jdbc, String sql, Object... args) {
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < args.length; i++) {
                statement.setObject(i + 1, args[i]);
            }
            return statement;
        }, keys);
        Number key = keys.getKey();
        if (key == null) {
            throw new IllegalStateException("The insert did not return a generated id");
        }
        return key.longValue();
    }

    /** Runs a COUNT-style query and returns 0 when the database returns no value. */
    static long count(JdbcTemplate jdbc, String sql, Object... args) {
        Long value = jdbc.queryForObject(sql, Long.class, args);
        return value == null ? 0L : value;
    }

    /** Runs a SUM-style query over a money column and returns a two-decimal amount. */
    static BigDecimal money(JdbcTemplate jdbc, String sql, Object... args) {
        Double value = jdbc.queryForObject(sql, Double.class, args);
        return toMoney(value);
    }

    static BigDecimal toMoney(Double value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }

    static Double toDb(BigDecimal amount) {
        return amount == null ? null : amount.setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
