# Online Reservation System

A desktop train/transport ticket booking application built with **Java 17+, Swing, JDBC and SQLite**.
Users log in, book tickets (a unique PNR is generated for every booking) and cancel bookings by PNR.
No database server is needed: data lives in a single `reservation.db` file created next to where you run the app.

## Feature checklist

| Area | Feature | Status |
|------|---------|--------|
| Login | Username + `JPasswordField`, credentials checked with `PreparedStatement` against SHA-256 hashes | Done |
| Login | Empty-field blocking, "Access Denied: Invalid username or password" dialog + password cleared, Enter submits | Done |
| Booking | Passenger, train number (digits only), auto-filled read-only train name (on focus lost **and** Enter), class combo, date, source, destination | Done |
| Booking | "Train not found" feedback, unique PNR (`PNR` + 8 alphanumerics, checked against the DB), confirmation dialog, form cleared afterwards | Done |
| Booking | Buttons: Book Ticket, Clear, Back | Done |
| Cancellation | PNR lookup, read-only details, "No booking found for this PNR", Confirm button disabled until a booking is fetched | Done |
| Cancellation | Yes/No confirmation, delete, "Cancellation successful", form reset | Done |
| Validation | `Validator.java`: required fields (trimmed), numeric train number, strict `dd-MM-yyyy` real dates not in the past, letters-only name, source != destination | Done |
| Quality | `PreparedStatement` everywhere, try-with-resources, EDT start-up, friendly SQL error dialogs, Javadoc on every class | Done |

## Prerequisites

* JDK 17 or newer (`java -version`)
* Apache Maven 3.8+ (`mvn -version`) and internet access the first time (to download `sqlite-jdbc`)
* A desktop environment (this is a Swing GUI)

## Build and run

```bash
# Option 1 - fat JAR (recommended)
mvn clean package
java -jar target/online-reservation-system.jar

# Option 2 - straight from Maven (compile first, exec:java does not compile by itself)
mvn compile exec:java
```

`reservation.db` is created in the **current working directory** on first start, and the tables plus seed data are
created automatically. To start from scratch, close the app and delete `reservation.db`.
To use another file: `java -Dreservation.db=/some/file.db -jar target/online-reservation-system.jar`.

## Default login

| Username | Password |
|----------|----------|
| `admin`  | `admin123` |

The password is stored only as a SHA-256 hash.

## Seed trains

12621 Tamil Nadu Express, 12627 Karnataka Express, 12601 Chennai Mail, 12841 Coromandel Express,
12839 Chennai Howrah Mail, 12433 Chennai Rajdhani Express, 12951 Mumbai Rajdhani Express,
12723 Telangana Express, 12007 Chennai Mysuru Shatabdi Express, 12163 Chennai Express.

## Project structure

```
pom.xml
README.md / TESTING.md
src/main/resources/schema.sql        table definitions (run automatically)
src/main/java/com/reservation
  Main.java                          entry point: DB init + seed, then LoginForm on the EDT
  db/   DatabaseManager, DataSeeder  connections, schema creation, default data
  model/ User, Train, Reservation    plain data classes
  dao/  UserDAO, TrainDAO, ReservationDAO   all SQL lives here (PreparedStatement only)
  ui/   LoginForm, MainMenu, ReservationForm, CancellationForm, UiTheme
  util/ Validator, PasswordUtil      validation rules and SHA-256 hashing
```

## Architecture and design decisions

* **Layering.** `ui` -> `dao` -> `db`, with `model` shared and `util` as stateless helpers. The UI never touches
  JDBC, and DAOs never touch Swing, so each layer can be changed or tested independently.
* **SQL safety.** Every statement uses `PreparedStatement` with bound parameters; there is no string-built SQL
  (the only concatenated text is a fixed table name inside `DataSeeder`, never user input). All JDBC objects are
  opened in try-with-resources.
* **Short-lived connections.** SQLite connections are cheap, so each DAO method opens and closes its own connection.
  This avoids shared mutable connection state and leaked handles. Foreign keys are switched on in the JDBC URL.
* **Schema in one place.** `schema.sql` is a classpath resource parsed by `DatabaseManager`, with `CREATE TABLE IF NOT EXISTS`
  so start-up is idempotent. `DataSeeder` only inserts when a table is empty, inside one transaction.
* **Validation in one class.** `Validator` returns either `null` (valid) or a specific message, which the forms show in a
  dialog and use to put the cursor on the faulty field. Dates use `ResolverStyle.STRICT`, so `29-02-2027` and `31-04-2027` are rejected.
* **Booking integrity.** On "Book Ticket" the train is looked up again in the database (not trusted from the screen),
  the PNR is generated with `SecureRandom` and checked for uniqueness, and the `PRIMARY KEY` plus the foreign key on
  `train_number` act as a final safety net.
* **Dates.** Stored as ISO text (`yyyy-MM-dd`) so they sort correctly; shown to users as `dd-MM-yyyy`.
* **Passwords.** SHA-256 as specified. Note for production: an unsalted fast hash is weak; prefer bcrypt/Argon2 with a salt.
* **Threading.** All Swing code starts on the Event Dispatch Thread. Queries are tiny local SQLite reads, so they run on the
  EDT for simplicity; a larger database would call for a `SwingWorker`.
* **Navigation.** The main menu hides while a form is open and re-appears when the form is closed (via Back or the window X).
* **Look and feel.** The cross-platform (Metal) look and feel is used so the custom colour theme looks the same on every OS.

## Troubleshooting

* *"The SQLite JDBC driver was not found"* - run through Maven (`mvn compile exec:java`) or use the fat JAR; do not run the bare class files.
* *Database is locked* - make sure only one copy of the app is using the same `reservation.db`.
