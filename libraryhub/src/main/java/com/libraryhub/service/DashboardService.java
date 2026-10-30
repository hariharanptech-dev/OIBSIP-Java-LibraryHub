package com.libraryhub.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;

import com.libraryhub.config.AppProperties;
import com.libraryhub.model.DashboardStats;
import com.libraryhub.model.LoanView;
import com.libraryhub.repository.FineRepository;
import com.libraryhub.repository.IssueRepository;
import com.libraryhub.repository.StatsRepository;
import com.libraryhub.util.DateUtil;

/** Builds the numbers shown on the home page and on the member and admin dashboards. */
@Service
public class DashboardService {

    /** A loan counts as "due soon" when it is due within this many days. */
    public static final int DUE_SOON_DAYS = 3;

    private final IssueRepository issues;
    private final FineRepository fines;
    private final StatsRepository stats;
    private final AppProperties properties;
    private final DateUtil dates;

    public DashboardService(IssueRepository issues, FineRepository fines, StatsRepository stats,
                            AppProperties properties, DateUtil dates) {
        this.issues = issues;
        this.fines = fines;
        this.stats = stats;
        this.properties = properties;
        this.dates = dates;
    }

    public LibraryTotals libraryTotals() {
        return new LibraryTotals(stats.titleCount(), stats.copyCount(), stats.memberCount());
    }

    public DashboardStats adminStats() {
        return stats.dashboardStats(dates.today());
    }

    public UserDashboard userDashboard(long userId) {
        LocalDate today = dates.today();
        List<LoanItem> items = issues.findActiveByUser(userId).stream()
                .map(loan -> toItem(loan, today))
                .toList();
        int overdue = (int) items.stream().filter(LoanItem::overdue).count();
        int dueSoon = (int) items.stream().filter(LoanItem::dueSoon).count();
        return new UserDashboard(items.size(), dueSoon, overdue, fines.sumUnpaidByUser(userId),
                properties.getFinePerDay(), items);
    }

    private LoanItem toItem(LoanView loan, LocalDate today) {
        long daysLeft = ChronoUnit.DAYS.between(today, loan.dueDate());
        boolean overdue = daysLeft < 0;
        boolean dueSoon = !overdue && daysLeft <= DUE_SOON_DAYS;
        BigDecimal projected = overdue
                ? properties.getFinePerDay().multiply(BigDecimal.valueOf(-daysLeft))
                : BigDecimal.ZERO;
        return new LoanItem(loan, daysLeft, overdue, dueSoon, projected);
    }

    public record LibraryTotals(long titles, long copies, long members) {
    }

    public record UserDashboard(int activeLoans, int dueSoon, int overdue, BigDecimal unpaidFines,
                                BigDecimal finePerDay, List<LoanItem> loans) {
    }

    /** One active loan with the display state computed for today. */
    public record LoanItem(LoanView loan, long daysLeft, boolean overdue, boolean dueSoon,
                           BigDecimal projectedFine) {

        /** Badge colour family: danger, warning or success. */
        public String badge() {
            return overdue ? "danger" : dueSoon ? "warning" : "success";
        }

        /** Icon name from the sprite. Status is always shown with an icon and text, never colour alone. */
        public String icon() {
            return overdue ? "alert" : dueSoon ? "clock" : "check";
        }

        public String label() {
            if (overdue) {
                long days = -daysLeft;
                return "Overdue by " + days + (days == 1 ? " day" : " days");
            }
            if (daysLeft == 0) {
                return "Due today";
            }
            if (daysLeft == 1) {
                return "Due tomorrow";
            }
            return "Due in " + daysLeft + " days";
        }
    }
}
