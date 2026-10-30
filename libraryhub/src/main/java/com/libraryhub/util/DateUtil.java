package com.libraryhub.util;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

import org.springframework.stereotype.Component;

/**
 * Date helper backed by the injected Clock, so "today" is controlled in one place and tests can
 * fix it. Also holds the static helpers used to read and write ISO-8601 text columns.
 * Exposed to templates as the bean "dateUtil".
 */
@Component("dateUtil")
public class DateUtil {

    private static final DateTimeFormatter DISPLAY_DATE =
            DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter DISPLAY_DATE_TIME =
            DateTimeFormatter.ofPattern("d MMM yyyy, h:mm a", Locale.ENGLISH);
    private static final DateTimeFormatter DB_DATE_TIME = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final Clock clock;

    public DateUtil(Clock clock) {
        this.clock = clock;
    }

    public LocalDate today() {
        return LocalDate.now(clock);
    }

    public LocalDateTime now() {
        return LocalDateTime.now(clock).truncatedTo(ChronoUnit.SECONDS);
    }

    /** Whole days from today until the given date. Negative when the date has passed. */
    public long daysUntil(LocalDate date) {
        return ChronoUnit.DAYS.between(today(), date);
    }

    /** Display format, for example "12 Nov 2026". */
    public String date(LocalDate date) {
        return date == null ? "-" : DISPLAY_DATE.format(date);
    }

    /** Display format, for example "12 Nov 2026, 3:30 PM". */
    public String dateTime(LocalDateTime dateTime) {
        return dateTime == null ? "-" : DISPLAY_DATE_TIME.format(dateTime);
    }

    public static String toDb(LocalDate date) {
        return date == null ? null : date.toString();
    }

    /** Always writes seconds, so text comparison in SQL matches chronological order. */
    public static String toDb(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.truncatedTo(ChronoUnit.SECONDS).format(DB_DATE_TIME);
    }

    public static LocalDate parseDate(String value) {
        return value == null || value.isBlank() ? null : LocalDate.parse(value);
    }

    public static LocalDateTime parseDateTime(String value) {
        return value == null || value.isBlank() ? null : LocalDateTime.parse(value);
    }
}
