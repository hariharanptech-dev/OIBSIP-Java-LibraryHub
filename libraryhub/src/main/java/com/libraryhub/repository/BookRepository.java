package com.libraryhub.repository;

import java.time.LocalDateTime;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.libraryhub.util.DateUtil;

@Repository
public class BookRepository {

    private final JdbcTemplate jdbc;

    public BookRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public long insert(String title, String author, String isbn, long categoryId, String description,
                       String publisher, Integer year, int quantity, LocalDateTime createdAt) {
        return JdbcSupport.insert(jdbc,
                "INSERT INTO books (title, author, isbn, category_id, description, publisher, year, "
                        + "total_quantity, available_quantity, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                title, author, isbn, categoryId, description, publisher, year, quantity, quantity,
                DateUtil.toDb(createdAt));
    }

    public boolean existsByIsbn(String isbn) {
        return JdbcSupport.count(jdbc, "SELECT COUNT(*) FROM books WHERE isbn = ?", isbn) > 0;
    }

    /**
     * Moves the available count by the given delta. The CHECK constraint on the table rejects
     * any change that would take it below zero or above the total.
     */
    public int adjustAvailable(long bookId, int delta) {
        return jdbc.update("UPDATE books SET available_quantity = available_quantity + ? WHERE id = ?",
                delta, bookId);
    }
}
