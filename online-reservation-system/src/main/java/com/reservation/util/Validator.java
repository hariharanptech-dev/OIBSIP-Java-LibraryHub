package com.reservation.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.regex.Pattern;

/**
 * Central place for all input validation. Every {@code validate*} method returns
 * {@code null} when the input is acceptable, otherwise a specific, user-friendly
 * error message that can be shown directly in a dialog.
 */
public final class Validator {

    /** Strict dd-MM-yyyy formatter (rejects 31-02-2027, 5-1-2027, etc.). */
    public static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-uuuu").withResolverStyle(ResolverStyle.STRICT);

    /** Format of a PNR: the literal "PNR" followed by 8 upper-case letters/digits. */
    public static final Pattern PNR_PATTERN = Pattern.compile("PNR[A-Z0-9]{8}");

    private static final Pattern DATE_SHAPE = Pattern.compile("\\d{2}-\\d{2}-\\d{4}");
    private static final Pattern LETTERS_AND_SPACES = Pattern.compile("[\\p{L} ]+");
    private static final Pattern STATION = Pattern.compile("\\p{L}[\\p{L} .'-]*");
    private static final Pattern DIGITS = Pattern.compile("[0-9]+");
    private static final int MAX_TRAIN_DIGITS = 9;
    private static final int MAX_TEXT_LENGTH = 60;

    private Validator() {
    }

    /**
     * Trims the text and collapses runs of whitespace into single spaces.
     *
     * @param text raw text (may be null)
     * @return normalised text, never null
     */
    public static String normalize(String text) {
        return text == null ? "" : text.trim().replaceAll("\\s+", " ");
    }

    /**
     * Validates the login form input.
     *
     * @param username entered username
     * @param password entered password
     * @return an error message or null if both fields are filled in
     */
    public static String validateLogin(String username, String password) {
        boolean noUser = username == null || username.trim().isEmpty();
        boolean noPass = password == null || password.isEmpty();
        if (noUser && noPass) {
            return "Please enter your username and password.";
        }
        if (noUser) {
            return "Please enter your username.";
        }
        if (noPass) {
            return "Please enter your password.";
        }
        return null;
    }

    /**
     * Validates a passenger name (letters and spaces only).
     *
     * @param name entered name
     * @return an error message or null if valid
     */
    public static String validatePassengerName(String name) {
        String value = normalize(name);
        if (value.isEmpty()) {
            return "Passenger name is required.";
        }
        if (!LETTERS_AND_SPACES.matcher(value).matches()) {
            return "Passenger name may contain letters and spaces only.";
        }
        if (value.length() > MAX_TEXT_LENGTH) {
            return "Passenger name must be at most " + MAX_TEXT_LENGTH + " characters.";
        }
        return null;
    }

    /**
     * Validates a train number (digits only).
     *
     * @param number entered train number
     * @return an error message or null if valid
     */
    public static String validateTrainNumber(String number) {
        String value = number == null ? "" : number.trim();
        if (value.isEmpty()) {
            return "Train number is required.";
        }
        if (!DIGITS.matcher(value).matches()) {
            return "Train number must contain digits only.";
        }
        if (value.length() > MAX_TRAIN_DIGITS) {
            return "Train number is too long (maximum " + MAX_TRAIN_DIGITS + " digits).";
        }
        return null;
    }

    /**
     * Validates the date of journey against today's date.
     *
     * @param text entered date in dd-MM-yyyy format
     * @return an error message or null if valid
     */
    public static String validateJourneyDate(String text) {
        return validateJourneyDate(text, LocalDate.now());
    }

    /**
     * Validates the date of journey against a given "today" (useful for testing).
     *
     * @param text  entered date in dd-MM-yyyy format
     * @param today the reference date
     * @return an error message or null if valid
     */
    public static String validateJourneyDate(String text, LocalDate today) {
        String value = text == null ? "" : text.trim();
        if (value.isEmpty()) {
            return "Date of journey is required.";
        }
        if (!DATE_SHAPE.matcher(value).matches()) {
            return "Date must be in dd-MM-yyyy format (for example 25-12-2026).";
        }
        LocalDate date;
        try {
            date = LocalDate.parse(value, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            return "\"" + value + "\" is not a real calendar date.";
        }
        if (date.isBefore(today)) {
            return "Date of journey cannot be in the past.";
        }
        return null;
    }

    /**
     * Parses a journey date that has already passed {@link #validateJourneyDate(String)}.
     *
     * @param text date in dd-MM-yyyy format
     * @return the parsed date
     * @throws DateTimeParseException if the text is not a valid date
     */
    public static LocalDate parseJourneyDate(String text) {
        return LocalDate.parse(text.trim(), DATE_FORMAT);
    }

    /**
     * Validates one station name.
     *
     * @param label friendly field name used in messages ("Source station")
     * @param value entered station name
     * @return an error message or null if valid
     */
    public static String validateStation(String label, String value) {
        String station = normalize(value);
        if (station.isEmpty()) {
            return label + " is required.";
        }
        if (!STATION.matcher(station).matches()) {
            return label + " may contain letters, spaces, dots, hyphens and apostrophes only.";
        }
        if (station.length() > MAX_TEXT_LENGTH) {
            return label + " must be at most " + MAX_TEXT_LENGTH + " characters.";
        }
        return null;
    }

    /**
     * Ensures the source and destination are different (case-insensitive).
     *
     * @param source      source station
     * @param destination destination station
     * @return an error message or null if they differ
     */
    public static String validateDifferentStations(String source, String destination) {
        if (normalize(source).equalsIgnoreCase(normalize(destination))) {
            return "Source and destination stations must be different.";
        }
        return null;
    }

    /**
     * Validates a PNR entered for lookup or cancellation.
     *
     * @param pnr entered PNR
     * @return an error message or null if well formed
     */
    public static String validatePnr(String pnr) {
        String value = pnr == null ? "" : pnr.trim().toUpperCase();
        if (value.isEmpty()) {
            return "Please enter a PNR number.";
        }
        if (!PNR_PATTERN.matcher(value).matches()) {
            return "A PNR looks like PNR followed by 8 letters or digits (for example PNR4K9X2M7A).";
        }
        return null;
    }
}
