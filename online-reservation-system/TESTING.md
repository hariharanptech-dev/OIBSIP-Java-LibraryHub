# Manual Test Cases

Start with a fresh database (delete `reservation.db`) and launch the app. Default login: `admin` / `admin123`.
Replace dates below with a real future date; "today" means the current date.

| # | Area | Steps | Expected result |
|---|------|-------|-----------------|
| 1 | Login | Enter `admin` / `admin123`, click Login | Login window closes, Main Menu opens showing "Logged in as admin" |
| 2 | Login | Enter `admin` / `wrong` | Dialog "Access Denied: Invalid username or password"; password field is cleared and focused; user stays on login |
| 3 | Login | Enter `nobody` / `admin123` | Same "Access Denied" dialog |
| 4 | Login | Leave both fields empty, click Login | Warning "Please enter your username and password."; no database check |
| 5 | Login | Username only, empty password | Warning "Please enter your password." |
| 6 | Login | Type `admin`, press Enter, type `admin123`, press Enter | Enter in username moves to password; Enter in password logs in |
| 7 | Login | Username `admin' OR '1'='1`, any password | "Access Denied" (input is treated as plain text, no injection) |
| 8 | Menu | Click Book a Ticket, then Back | Booking form opens and menu hides; Back returns to the menu |
| 9 | Menu | Click Logout | Menu closes and the login window opens again |
| 10 | Booking | Type letters or symbols into Train Number | Characters are rejected; only digits are accepted (max 9) |
| 11 | Booking | Enter train `12621`, press Enter | Train Name shows "Tamil Nadu Express" (read-only), status "Train found" |
| 12 | Booking | Enter `12627`, then click/tab to another field | Train Name auto-fills "Karnataka Express" on focus lost |
| 13 | Booking | Enter `99999`, press Enter | Train Name empty, red status "Train not found" |
| 14 | Booking | Fill every field except Passenger Name, click Book Ticket | Warning "Passenger name is required."; cursor moves to that field |
| 15 | Booking | Passenger Name `Ravi123` | Warning "Passenger name may contain letters and spaces only." |
| 16 | Booking | Date `2026-12-25` or `5-1-2027` | Warning "Date must be in dd-MM-yyyy format (for example 25-12-2026)." |
| 17 | Booking | Date `29-02-2027` or `31-04-2027` | Warning that it is not a real calendar date |
| 18 | Booking | Date = yesterday | Warning "Date of journey cannot be in the past." |
| 19 | Booking | Date = today | Accepted (today is allowed) |
| 20 | Booking | Source `Chennai`, Destination ` chennai ` | Warning "Source and destination stations must be different." |
| 21 | Booking | Train number `99999` valid in every other field, click Book Ticket | Warning "Train not found..."; nothing saved |
| 22 | Booking | Valid data: `Ravi Kumar`, `12621`, Sleeper, future date, `Chennai` -> `Delhi`, Book Ticket | Confirmation dialog with PNR (`PNR` + 8 characters), passenger, train number and name, class, date, source, destination; form is cleared |
| 23 | Booking | Click Clear after typing in several fields | All fields, status and train name reset; class returns to Sleeper |
| 24 | PNR uniqueness | Book 5 tickets in a row | Five different PNRs, all in the form `PNR[A-Z0-9]{8}` |
| 25 | Cancel | Open Cancel a Ticket | "Confirm Cancellation" is disabled; detail fields empty |
| 26 | Cancel | Enter a PNR from test 22 (any case), click Fetch Details | Details shown (passenger, train, class, date, route, booked by, booked at); Confirm enabled |
| 27 | Cancel | Enter `PNRZZZZZZZZ`, Fetch Details | "No booking found for this PNR"; Confirm stays disabled |
| 28 | Cancel | Enter `ABC` or leave empty, Fetch Details | Warning about the PNR format / "Please enter a PNR number." |
| 29 | Cancel | Fetch a valid PNR, then edit the PNR text | Displayed details clear and Confirm becomes disabled again |
| 30 | Cancel | Fetch valid PNR, Confirm Cancellation, choose **No** | Nothing deleted; booking still fetchable afterwards |
| 31 | Cancel | Fetch valid PNR, Confirm Cancellation, choose **Yes** | "Cancellation successful"; form resets |
| 32 | Cancel | Fetch the same PNR again after test 31 | "No booking found for this PNR" |
| 33 | Persistence | Book a ticket, close the app, relaunch, fetch that PNR in Cancel | Booking is still there (data in `reservation.db`) |
| 34 | Window close | Close the Booking form with the window X | Main Menu re-appears |
| 35 | DB error | Close the app, make `reservation.db` read-only or locked by another process, retry a booking | A friendly "Database Error" dialog appears; no stack trace is shown |
