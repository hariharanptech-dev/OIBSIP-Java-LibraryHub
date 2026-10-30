package com.libraryhub.config;

import java.math.BigDecimal;
import java.time.ZoneId;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Library rules, bound from the "library.*" keys in application.properties. */
@Validated
@ConfigurationProperties(prefix = "library")
public class AppProperties {

    @Min(1)
    private int loanDays = 14;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal finePerDay = new BigDecimal("5.00");

    @Min(1)
    private int maxActiveLoans = 3;

    private boolean blockIssueWithUnpaidFines = true;

    @Min(1)
    private int reservationHoldHours = 48;

    @NotBlank
    private String timezone = "Asia/Kolkata";

    private boolean showDemoCredentials = true;

    @Valid
    private final Login login = new Login();

    public int getLoanDays() {
        return loanDays;
    }

    public void setLoanDays(int loanDays) {
        this.loanDays = loanDays;
    }

    public BigDecimal getFinePerDay() {
        return finePerDay;
    }

    public void setFinePerDay(BigDecimal finePerDay) {
        this.finePerDay = finePerDay;
    }

    public int getMaxActiveLoans() {
        return maxActiveLoans;
    }

    public void setMaxActiveLoans(int maxActiveLoans) {
        this.maxActiveLoans = maxActiveLoans;
    }

    public boolean isBlockIssueWithUnpaidFines() {
        return blockIssueWithUnpaidFines;
    }

    public void setBlockIssueWithUnpaidFines(boolean blockIssueWithUnpaidFines) {
        this.blockIssueWithUnpaidFines = blockIssueWithUnpaidFines;
    }

    public int getReservationHoldHours() {
        return reservationHoldHours;
    }

    public void setReservationHoldHours(int reservationHoldHours) {
        this.reservationHoldHours = reservationHoldHours;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public boolean isShowDemoCredentials() {
        return showDemoCredentials;
    }

    public void setShowDemoCredentials(boolean showDemoCredentials) {
        this.showDemoCredentials = showDemoCredentials;
    }

    public Login getLogin() {
        return login;
    }

    public ZoneId zoneId() {
        return ZoneId.of(timezone);
    }

    /** Sign-in rate limiting. */
    public static class Login {

        @Min(1)
        private int maxFailedAttempts = 5;

        @Min(1)
        private int lockSeconds = 60;

        public int getMaxFailedAttempts() {
            return maxFailedAttempts;
        }

        public void setMaxFailedAttempts(int maxFailedAttempts) {
            this.maxFailedAttempts = maxFailedAttempts;
        }

        public int getLockSeconds() {
            return lockSeconds;
        }

        public void setLockSeconds(int lockSeconds) {
            this.lockSeconds = lockSeconds;
        }
    }
}
