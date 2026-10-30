package com.libraryhub.model;

import java.math.BigDecimal;

/** Headline numbers for the admin dashboard. */
public record DashboardStats(
        long totalTitles,
        long totalCopies,
        long issuedNow,
        long overdue,
        long members,
        BigDecimal unpaidFinesTotal,
        long newQueries) {
}
