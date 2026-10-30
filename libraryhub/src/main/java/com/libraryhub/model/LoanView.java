package com.libraryhub.model;

import java.time.LocalDate;

/** One active loan joined with its book, as shown on dashboards. */
public record LoanView(
        long issueId,
        long bookId,
        String title,
        String author,
        LocalDate issueDate,
        LocalDate dueDate) {
}
