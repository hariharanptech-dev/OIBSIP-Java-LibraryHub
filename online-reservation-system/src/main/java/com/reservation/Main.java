package com.reservation;

import com.reservation.db.DataSeeder;
import com.reservation.db.DatabaseManager;
import com.reservation.ui.LoginForm;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.sql.SQLException;

/**
 * Application entry point. Prepares the database (schema + seed data) and
 * then opens the {@link LoginForm} on the Event Dispatch Thread.
 */
public final class Main {

    private Main() {
    }

    /**
     * Launches the application.
     *
     * @param args ignored
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Cross-platform look and feel so the custom colour theme renders identically everywhere.
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignored) {
                // Falls back to the default look and feel.
            }
            try {
                DatabaseManager.initialize();
                DataSeeder.seedIfEmpty();
            } catch (SQLException ex) {
                System.err.println("Database initialisation failed: " + ex.getMessage());
                JOptionPane.showMessageDialog(null,
                        "The application could not open its database.\n\n" + ex.getMessage(),
                        "Database Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
                return;
            }
            new LoginForm().setVisible(true);
        });
    }
}
