package com.libraryhub.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import com.libraryhub.config.AppProperties;
import com.libraryhub.model.IssueStatus;
import com.libraryhub.model.QueryStatus;
import com.libraryhub.model.Role;
import com.libraryhub.repository.BookRepository;
import com.libraryhub.repository.CategoryRepository;
import com.libraryhub.repository.FineRepository;
import com.libraryhub.repository.IssueRepository;
import com.libraryhub.repository.QueryRepository;
import com.libraryhub.repository.UserRepository;
import com.libraryhub.util.DateUtil;
import com.libraryhub.util.IsbnValidator;

/**
 * Loads demo data on first start. It runs only when the users table is empty and does all its
 * work in one transaction, so a failure leaves the database empty and the next start tries again.
 * Dates are relative to today, so the overdue loan is always overdue.
 */
@Component
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private static final String BOOKS_RESOURCE = "seed/books.csv";

    private static final List<String> CATEGORIES = List.of(
            "Computer Science", "Fiction", "Science", "History",
            "Biography", "Business", "Self-Help", "Mathematics");

    // ISBNs of the books used in the demo scenarios (all present in seed/books.csv).
    private static final String CLEAN_CODE = "9780132350884";
    private static final String PYTHON_CRASH_COURSE = "9781593279288";
    private static final String ATOMIC_HABITS = "9780735211292";
    private static final String THINKING_FAST_SLOW = "9780374533557";
    private static final String SAPIENS = "9780062316110";
    private static final String LEAN_STARTUP = "9780307887894";
    private static final String DEEP_WORK = "9781455586691";

    /** Shown on the sign-in page when library.show-demo-credentials is true. */
    public record DemoAccount(String role, String email, String password) {
    }

    public static final List<DemoAccount> DEMO_ACCOUNTS = List.of(
            new DemoAccount("Admin", "admin@libraryhub.com", "Admin@123"),
            new DemoAccount("Member", "demo@libraryhub.com", "Demo@1234"),
            new DemoAccount("Member", "priya@libraryhub.com", "User@1234"),
            new DemoAccount("Member", "rahul@libraryhub.com", "User@1234"));

    private final UserRepository users;
    private final CategoryRepository categories;
    private final BookRepository books;
    private final IssueRepository issues;
    private final FineRepository fines;
    private final QueryRepository queries;
    private final PasswordEncoder passwordEncoder;
    private final DateUtil dates;
    private final AppProperties properties;
    private final TransactionTemplate transaction;

    public DataSeeder(UserRepository users, CategoryRepository categories, BookRepository books,
                      IssueRepository issues, FineRepository fines, QueryRepository queries,
                      PasswordEncoder passwordEncoder, DateUtil dates, AppProperties properties,
                      TransactionTemplate transaction) {
        this.users = users;
        this.categories = categories;
        this.books = books;
        this.issues = issues;
        this.fines = fines;
        this.queries = queries;
        this.passwordEncoder = passwordEncoder;
        this.dates = dates;
        this.properties = properties;
        this.transaction = transaction;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (users.count() > 0) {
            log.info("Database already has data, so seeding is skipped.");
            return;
        }
        transaction.executeWithoutResult(status -> seed());
        log.info("Demo data loaded. Sign in as admin@libraryhub.com (Admin@123) or demo@libraryhub.com (Demo@1234).");
    }

    private void seed() {
        LocalDate today = dates.today();
        LocalDateTime now = dates.now();

        Map<String, Long> categoryIds = new HashMap<>();
        for (String name : CATEGORIES) {
            categoryIds.put(name, categories.insert(name));
        }
        Map<String, Long> bookIds = loadBooks(categoryIds, now.minusDays(60));

        users.insert("Library Admin", "admin@libraryhub.com", hash("Admin@123"), Role.ADMIN, true, now.minusDays(90));
        long demoId = users.insert("Anika Sharma", "demo@libraryhub.com", hash("Demo@1234"), Role.USER, true, now.minusDays(45));
        long priyaId = users.insert("Priya Nair", "priya@libraryhub.com", hash("User@1234"), Role.USER, true, now.minusDays(40));
        long rahulId = users.insert("Rahul Verma", "rahul@libraryhub.com", hash("User@1234"), Role.USER, true, now.minusDays(35));

        // Anika: one overdue loan (returning it creates a fine), one current loan, one finished loan.
        issue(bookIds.get(CLEAN_CODE), demoId, today.minusDays(24), null);
        issue(bookIds.get(ATOMIC_HABITS), demoId, today.minusDays(8), null);
        issue(bookIds.get(PYTHON_CRASH_COURSE), demoId, today.minusDays(30), today.minusDays(20));

        // Priya holds the only copy of a one-copy title, so other members see "Reserve" for it.
        issue(bookIds.get(THINKING_FAST_SLOW), priyaId, today.minusDays(3), null);
        issue(bookIds.get(SAPIENS), priyaId, today.minusDays(35), today.minusDays(25));

        // Rahul returned a book 6 days late, so he has an unpaid fine and cannot borrow until it is paid.
        long lateIssue = issue(bookIds.get(LEAN_STARTUP), rahulId, today.minusDays(40), today.minusDays(20));
        int daysLate = (int) ChronoUnit.DAYS.between(today.minusDays(40).plusDays(properties.getLoanDays()),
                today.minusDays(20));
        if (daysLate > 0) {
            BigDecimal amount = properties.getFinePerDay().multiply(BigDecimal.valueOf(daysLate));
            fines.insert(lateIssue, rahulId, daysLate, amount, false,
                    today.minusDays(20).atTime(11, 0), null, null);
        }
        issue(bookIds.get(DEEP_WORK), rahulId, today.minusDays(5), null);

        queries.insert(demoId, "Anika Sharma", "demo@libraryhub.com",
                "Can I extend my loan period?",
                "Hello, I am reading a book that I will not finish by the due date. "
                        + "Can the loan be extended, or should I return it and borrow it again?",
                QueryStatus.NEW, null, now.minusDays(2), null);
        queries.insert(priyaId, "Priya Nair", "priya@libraryhub.com",
                "Request to add a title to the catalogue",
                "Could the library add \"Algorithms to Live By\"? It would be very useful for our study group.",
                QueryStatus.RESOLVED,
                "Thank you for the suggestion. We have added it to the acquisitions list and will update the catalogue when a copy arrives.",
                now.minusDays(6), now.minusDays(5));
    }

    private String hash(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    /** Records a loan. Open loans take one copy out of stock. Returns the issue id. */
    private long issue(Long bookId, long userId, LocalDate issueDate, LocalDate returnDate) {
        if (bookId == null) {
            throw new IllegalStateException("A demo scenario refers to a book that is not in " + BOOKS_RESOURCE);
        }
        LocalDate due = issueDate.plusDays(properties.getLoanDays());
        IssueStatus status = returnDate == null ? IssueStatus.ISSUED : IssueStatus.RETURNED;
        long id = issues.insert(bookId, userId, issueDate, due, returnDate, status);
        if (status == IssueStatus.ISSUED) {
            books.adjustAvailable(bookId, -1);
        }
        return id;
    }

    /** Reads seed/books.csv (pipe separated, UTF-8) and returns ISBN to book id. */
    private Map<String, Long> loadBooks(Map<String, Long> categoryIds, LocalDateTime firstCreatedAt) {
        Map<String, Long> ids = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new ClassPathResource(BOOKS_RESOURCE).getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (lineNumber == 1 || line.isBlank()) {
                    continue;
                }
                String[] c = line.split("\\|", -1);
                if (c.length != 8) {
                    throw new IllegalStateException(BOOKS_RESOURCE + " line " + lineNumber + " needs 8 columns");
                }
                String isbn = IsbnValidator.normalize(c[3]);
                if (!IsbnValidator.isValid(isbn)) {
                    throw new IllegalStateException(BOOKS_RESOURCE + " line " + lineNumber + " has an invalid ISBN");
                }
                Long categoryId = categoryIds.get(c[0].trim());
                if (categoryId == null) {
                    throw new IllegalStateException(BOOKS_RESOURCE + " line " + lineNumber + " has an unknown category");
                }
                long id = books.insert(c[1].trim(), c[2].trim(), isbn, categoryId, c[7].trim(), c[5].trim(),
                        Integer.valueOf(c[4].trim()), Integer.parseInt(c[6].trim()),
                        firstCreatedAt.plusMinutes(lineNumber));
                ids.put(isbn, id);
            }
        } catch (IOException ex) {
            throw new UncheckedIOException("Could not read " + BOOKS_RESOURCE, ex);
        }
        return ids;
    }
}
