# LibraryHub: Digital Library Management System (Phase 1)

Java 17+, Spring Boot 3.3, Thymeleaf, Spring Security, Spring JDBC and SQLite. No CDN, no external fonts, works offline.

This is the Phase 1 build: project skeleton, database, seed data, security, sign-in, registration, role redirects,
and the member and admin dashboards. Books, loans, fines, reservations and the contact inbox arrive in Phase 2.

## Run

Needs JDK 17 or newer (`java -version`). Maven is optional because the wrapper downloads it once.

    ./mvnw spring-boot:run          (Linux, macOS)
    mvnw.cmd spring-boot:run        (Windows)
    mvn spring-boot:run             (if Maven is installed)

Open http://localhost:8080

Build a runnable JAR and run it:

    mvn clean package
    java -jar target/libraryhub.jar
    java -jar target/libraryhub.jar --server.port=9090

`library.db` is created in the folder you start the app from. Delete it (and `library.db-shm`, `library.db-wal`)
to reset the demo data.

## Demo accounts

| Role   | Email                  | Password   | What to try                                              |
|--------|------------------------|------------|----------------------------------------------------------|
| Admin  | admin@libraryhub.com   | Admin@123  | Lands on /admin with live stat cards                     |
| Member | demo@libraryhub.com    | Demo@1234  | One overdue loan (10 days), one current loan             |
| Member | priya@libraryhub.com   | User@1234  | Holds the only copy of "Thinking, Fast and Slow"         |
| Member | rahul@libraryhub.com   | User@1234  | Has an unpaid fine of Rs 30.00 and one current loan      |

## Configuration (src/main/resources/application.properties)

| Key                                   | Default       | Meaning                                         |
|---------------------------------------|---------------|-------------------------------------------------|
| server.port                           | 8080          | HTTP port                                       |
| library.loan-days                     | 14            | Loan period                                     |
| library.fine-per-day                  | 5.00          | Fine per overdue day (rupees)                   |
| library.max-active-loans              | 3             | Books a member can hold at once                 |
| library.block-issue-with-unpaid-fines | true          | Unpaid fines block new loans                    |
| library.reservation-hold-hours        | 48            | How long a returned copy is held for a reserver |
| library.timezone                      | Asia/Kolkata  | Time zone for all dates                         |
| library.show-demo-credentials         | true          | Show demo accounts on the sign-in page          |
| library.login.max-failed-attempts     | 5             | Failed sign-ins before an email is locked       |
| library.login.lock-seconds            | 60            | Lock length                                     |

## Troubleshooting

* Port in use: run with `--server.port=9090`.
* `JAVA_HOME` or "release version 17 not supported": install JDK 17+ and point JAVA_HOME at it.
* "database is locked": another copy of the app is running against the same `library.db`. Stop it.
* On Windows, if `./mvnw` fails inside WSL or Git Bash with `\r` errors, the file picked up Windows line
  endings. Run `dos2unix mvnw`, or use `mvnw.cmd`.
