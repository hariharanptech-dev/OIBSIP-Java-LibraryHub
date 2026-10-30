package com.reservation.dao;

import com.reservation.db.DatabaseManager;
import com.reservation.model.Train;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/** Data access for the {@code trains} table, plus the list of bookable classes. */
public class TrainDAO {

    private static final List<String> CLASSES =
            List.of("Sleeper", "Third AC", "Second AC", "First AC", "Chair Car");

    /**
     * Looks a train up by its number.
     *
     * @param trainNumber the train number
     * @return the train, or empty if no such train exists
     * @throws SQLException on database failure
     */
    public Optional<Train> findByNumber(int trainNumber) throws SQLException {
        String sql = "SELECT train_number, train_name FROM trains WHERE train_number = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, trainNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Train(rs.getInt("train_number"), rs.getString("train_name")));
                }
                return Optional.empty();
            }
        }
    }

    /**
     * Lists the travel classes offered by every train.
     *
     * @return an immutable list of class names, in display order
     */
    public List<String> listClasses() {
        return CLASSES;
    }
}
