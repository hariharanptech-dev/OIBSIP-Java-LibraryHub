-- Online Reservation System : SQLite schema
-- Executed automatically by DatabaseManager on first run (all statements are idempotent).
-- Note: one statement per ';' and no semicolons inside comments or literals.

CREATE TABLE IF NOT EXISTS users (
    user_id       INTEGER PRIMARY KEY AUTOINCREMENT,
    username      TEXT UNIQUE NOT NULL,
    password_hash TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS trains (
    train_number  INTEGER PRIMARY KEY,
    train_name    TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS reservations (
    pnr            TEXT PRIMARY KEY,
    passenger_name TEXT NOT NULL,
    train_number   INTEGER NOT NULL,
    train_name     TEXT NOT NULL,
    class_type     TEXT NOT NULL,
    journey_date   TEXT NOT NULL,
    source         TEXT NOT NULL,
    destination    TEXT NOT NULL,
    booked_by      TEXT NOT NULL,
    booked_at      TEXT DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (train_number) REFERENCES trains (train_number)
);
