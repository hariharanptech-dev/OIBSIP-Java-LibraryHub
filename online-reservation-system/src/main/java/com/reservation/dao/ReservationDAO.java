package com.reservation.dao;

import com.reservation.db.DatabaseManager;
import com.reservation.model.Reservation;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Data access for the {@code reservations} table. Journey dates are stored as
 * ISO-8601 text (yyyy-MM-dd) so they sort correctly, and are shown to users as dd-MM-yyyy.
 */
public class ReservationDAO {

    private static final String PNR_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int PNR_SUFFIX_LENGTH = 8;
    private static final int MAX_PNR_ATTEMPTS = 50;
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Generates a PNR ("PNR" + 8 random letters/digits) that is not yet in the database.
     *
     * @return a unique PNR
     * @throws SQLException on database failure, or if no unique PNR was found (practically impossible)
     */
    public String generateUniquePnr() throws SQLException {
        for (int attempt = 0; attempt < MAX_PNR_ATTEMPTS; attempt++) {
            StringBuilder sb = new StringBuilder("PNR");
            for (int i = 0; i < PNR_SUFFIX_LENGTH; i++) {
                sb.append(PNR_ALPHABET.charAt(RANDOM.nextInt(PNR_ALPHABET.length())));
            }
            String candidate = sb.toString();
            if (!pnrExists(candidate)) {
                return candidate;
            }
        }
        throw new SQLException("Could not generate a unique PNR. Please try again.");
    }

    /**
     * Tests whether a PNR is already used.
     *
     * @param pnr the PNR to look for
     * @return true if a booking with that PNR exists
     * @throws SQLException on database failure
     */
    public boolean pnrExists(String pnr) throws SQLException {
        String sql = "SELECT 1 FROM reservations WHERE pnr = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pnr);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Saves a new reservation.
     *
     * @param r the reservation to store
     * @throws SQLException on database failure (including a duplicate PNR)
     */
    public void insert(Reservation r) throws SQLException {
        String sql = "INSERT INTO reservations (pnr, passenger_name, train_number, train_name, class_type, "
                + "journey_date, source, destination, booked_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, r.getPnr());
            ps.setString(2, r.getPassengerName());
            ps.setInt(3, r.getTrainNumber());
            ps.setString(4, r.getTrainName());
            ps.setString(5, r.getClassType());
            ps.setString(6, r.getJourneyDate().toString());
            ps.setString(7, r.getSource());
            ps.setString(8, r.getDestination());
            ps.setString(9, r.getBookedBy());
            ps.executeUpdate();
        }
    }

    /**
     * Finds a booking by PNR.
     *
     * @param pnr the PNR (case-sensitive, stored upper-case)
     * @return the booking, or empty if none exists
     * @throws SQLException on database failure
     */
    public Optional<Reservation> findByPnr(String pnr) throws SQLException {
        String sql = "SELECT pnr, passenger_name, train_number, train_name, class_type, journey_date, "
                + "source, destination, booked_by, booked_at FROM reservations WHERE pnr = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pnr);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(new Reservation(
                        rs.getString("pnr"),
                        rs.getString("passenger_name"),
                        rs.getInt("train_number"),
                        rs.getString("train_name"),
                        rs.getString("class_type"),
                        LocalDate.parse(rs.getString("journey_date")),
                        rs.getString("source"),
                        rs.getString("destination"),
                        rs.getString("booked_by"),
                        rs.getString("booked_at")));
            }
        }
    }

    /**
     * Deletes a booking.
     *
     * @param pnr the PNR to cancel
     * @return true if a row was deleted, false if the PNR did not exist
     * @throws SQLException on database failure
     */
    public boolean deleteByPnr(String pnr) throws SQLException {
        String sql = "DELETE FROM reservations WHERE pnr = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pnr);
            return ps.executeUpdate() > 0;
        }
    }
}
