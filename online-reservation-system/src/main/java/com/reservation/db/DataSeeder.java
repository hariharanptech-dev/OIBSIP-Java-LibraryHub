package com.reservation.db;

import com.reservation.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** Inserts the default user and sample trains when the tables are empty. */
public final class DataSeeder {

    private static final String DEFAULT_USER = "admin";
    private static final String DEFAULT_PASSWORD = "admin123";

    private static final Object[][] TRAINS = {
            {12621, "Tamil Nadu Express"},
            {12627, "Karnataka Express"},
            {12601, "Chennai Mail"},
            {12841, "Coromandel Express"},
            {12839, "Chennai Howrah Mail"},
            {12433, "Chennai Rajdhani Express"},
            {12951, "Mumbai Rajdhani Express"},
            {12723, "Telangana Express"},
            {12007, "Chennai Mysuru Shatabdi Express"},
            {12163, "Chennai Express"}
    };

    private DataSeeder() {
    }

    /**
     * Seeds the default admin user and the sample trains, each only if its table is empty.
     * Runs in a single transaction so a failure leaves the database untouched.
     *
     * @throws SQLException if the database cannot be read or written
     */
    public static void seedIfEmpty() throws SQLException {
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (isEmpty(conn, "users")) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO users (username, password_hash) VALUES (?, ?)")) {
                        ps.setString(1, DEFAULT_USER);
                        ps.setString(2, PasswordUtil.sha256(DEFAULT_PASSWORD));
                        ps.executeUpdate();
                    }
                }
                if (isEmpty(conn, "trains")) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO trains (train_number, train_name) VALUES (?, ?)")) {
                        for (Object[] train : TRAINS) {
                            ps.setInt(1, (Integer) train[0]);
                            ps.setString(2, (String) train[1]);
                            ps.addBatch();
                        }
                        ps.executeBatch();
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /**
     * Checks whether a table has no rows. The table name comes from the fixed
     * literals above (never from user input), so it is safe to concatenate.
     */
    private static boolean isEmpty(Connection conn, String table) throws SQLException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + table)) {
            return rs.next() && rs.getInt(1) == 0;
        }
    }
}
