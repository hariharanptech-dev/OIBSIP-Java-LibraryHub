package com.libraryhub.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CategoryRepository {

    private final JdbcTemplate jdbc;

    public CategoryRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<Long> findIdByName(String name) {
        List<Long> ids = jdbc.queryForList("SELECT id FROM categories WHERE name = ?", Long.class, name);
        return ids.stream().findFirst();
    }

    public long insert(String name) {
        return JdbcSupport.insert(jdbc, "INSERT INTO categories (name) VALUES (?)", name);
    }

    /** Returns the id of the named category, creating it when it does not exist yet. */
    public long findOrCreate(String name) {
        return findIdByName(name).orElseGet(() -> insert(name));
    }
}
