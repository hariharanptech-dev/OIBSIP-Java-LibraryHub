-- LibraryHub schema. Every statement is idempotent so it can run on each start.
-- Dates are stored as ISO-8601 text: yyyy-MM-dd for dates, yyyy-MM-ddTHH:mm:ss for timestamps.

CREATE TABLE IF NOT EXISTS users (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    name          TEXT    NOT NULL,
    email         TEXT    NOT NULL UNIQUE COLLATE NOCASE,
    password_hash TEXT    NOT NULL,
    role          TEXT    NOT NULL CHECK (role IN ('ADMIN', 'USER')),
    active        INTEGER NOT NULL DEFAULT 1 CHECK (active IN (0, 1)),
    created_at    TEXT    NOT NULL
);

CREATE TABLE IF NOT EXISTS categories (
    id   INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE COLLATE NOCASE
);

CREATE TABLE IF NOT EXISTS books (
    id                 INTEGER PRIMARY KEY AUTOINCREMENT,
    title              TEXT    NOT NULL,
    author             TEXT    NOT NULL,
    isbn               TEXT    NOT NULL UNIQUE,
    category_id        INTEGER NOT NULL REFERENCES categories (id),
    description        TEXT,
    publisher          TEXT,
    year               INTEGER,
    total_quantity     INTEGER NOT NULL CHECK (total_quantity BETWEEN 1 AND 1000),
    available_quantity INTEGER NOT NULL,
    created_at         TEXT    NOT NULL,
    CHECK (available_quantity >= 0 AND available_quantity <= total_quantity)
);

CREATE TABLE IF NOT EXISTS issues (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    book_id     INTEGER NOT NULL REFERENCES books (id),
    user_id     INTEGER NOT NULL REFERENCES users (id),
    issue_date  TEXT    NOT NULL,
    due_date    TEXT    NOT NULL,
    return_date TEXT,
    status      TEXT    NOT NULL CHECK (status IN ('ISSUED', 'RETURNED')),
    CHECK ((status = 'ISSUED' AND return_date IS NULL) OR (status = 'RETURNED' AND return_date IS NOT NULL))
);

CREATE TABLE IF NOT EXISTS fines (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    issue_id     INTEGER NOT NULL UNIQUE REFERENCES issues (id),
    user_id      INTEGER NOT NULL REFERENCES users (id),
    days_overdue INTEGER NOT NULL CHECK (days_overdue > 0),
    amount       REAL    NOT NULL CHECK (amount >= 0),
    paid         INTEGER NOT NULL DEFAULT 0 CHECK (paid IN (0, 1)),
    created_at   TEXT    NOT NULL,
    paid_at      TEXT,
    paid_by      INTEGER REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS reservations (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    book_id         INTEGER NOT NULL REFERENCES books (id),
    user_id         INTEGER NOT NULL REFERENCES users (id),
    status          TEXT    NOT NULL CHECK (status IN ('WAITING', 'READY', 'FULFILLED', 'CANCELLED', 'EXPIRED')),
    created_at      TEXT    NOT NULL,
    ready_at        TEXT,
    hold_expires_at TEXT
);

CREATE TABLE IF NOT EXISTS queries (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id     INTEGER REFERENCES users (id),
    name        TEXT    NOT NULL,
    email       TEXT    NOT NULL,
    subject     TEXT    NOT NULL,
    message     TEXT    NOT NULL,
    status      TEXT    NOT NULL CHECK (status IN ('NEW', 'RESOLVED')),
    admin_reply TEXT,
    created_at  TEXT    NOT NULL,
    resolved_at TEXT
);

CREATE INDEX IF NOT EXISTS idx_books_title        ON books (title COLLATE NOCASE);
CREATE INDEX IF NOT EXISTS idx_books_author       ON books (author COLLATE NOCASE);
CREATE INDEX IF NOT EXISTS idx_books_category     ON books (category_id);
CREATE INDEX IF NOT EXISTS idx_issues_user        ON issues (user_id, status);
CREATE INDEX IF NOT EXISTS idx_issues_book        ON issues (book_id, status);
CREATE INDEX IF NOT EXISTS idx_issues_due         ON issues (status, due_date);
CREATE INDEX IF NOT EXISTS idx_reservations_book  ON reservations (book_id, status);
CREATE INDEX IF NOT EXISTS idx_reservations_user  ON reservations (user_id, status);
CREATE INDEX IF NOT EXISTS idx_fines_user         ON fines (user_id, paid);
CREATE INDEX IF NOT EXISTS idx_queries_status     ON queries (status, created_at);

-- A member can hold a given title only once at a time.
CREATE UNIQUE INDEX IF NOT EXISTS ux_issues_active_user_book
    ON issues (user_id, book_id) WHERE status = 'ISSUED';

-- A member can have only one active reservation per title.
CREATE UNIQUE INDEX IF NOT EXISTS ux_reservations_active_user_book
    ON reservations (user_id, book_id) WHERE status IN ('WAITING', 'READY');
