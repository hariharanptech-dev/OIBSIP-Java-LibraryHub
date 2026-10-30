package com.libraryhub.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.libraryhub.model.IssueStatus;
import com.libraryhub.model.LoanView;
import com.libraryhub.util.DateUtil;

@Repository
public class IssueRepository {

    private static final RowMapper<LoanView> LOAN_MAPPER = (rs, rowNum) -> new LoanView(
            rs.getLong("issue_id"),
            rs.getLong("book_id"),
            rs.getString("title"),
            rs.getString("author"),
            DateUtil.parseDate(rs.getString("issue_date")),
            DateUtil.parseDate(rs.getString("due_date")));

    private final JdbcTemplate jdbc;

    public IssueRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public long insert(long bookId, long userId, LocalDate issueDate, LocalDate dueDate,
                       LocalDate returnDate, IssueStatus status) {
        return JdbcSupport.insert(jdbc,
                "INSERT INTO issues (book_id, user_id, issue_date, due_date, return_date, status) "
                        + "VALUES (?, ?, ?, ?, ?, ?)",
                bookId, userId, DateUtil.toDb(issueDate), DateUtil.toDb(dueDate), DateUtil.toDb(returnDate),
                status.name());
    }

    /** Loans the member still holds, soonest due date first. */
    public List<LoanView> findActiveByUser(long userId) {
        return jdbc.query(
                "SELECT i.id AS issue_id, b.id AS book_id, b.title, b.author, i.issue_date, i.due_date "
                        + "FROM issues i JOIN books b ON b.id = i.book_id "
                        + "WHERE i.user_id = ? AND i.status = 'ISSUED' "
                        + "ORDER BY i.due_date ASC, i.id ASC",
                LOAN_MAPPER, userId);
    }
}
