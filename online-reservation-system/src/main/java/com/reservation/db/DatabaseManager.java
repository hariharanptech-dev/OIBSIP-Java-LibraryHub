package com.reservation.db;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles SQLite connections and creates the schema on first run.
 * <p>
 * The database file {@code reservation.db} lives in the current working directory
 * (override with {@code -Dreservation.db=<file>} if needed). Connections are cheap
 * for SQLite, so every DAO call opens its own short-lived connection and closes it
 * with try-with-resources.
 */
public final class DatabaseManager {

    private static final String DB_FILE = System.getProperty("reservation.db", "reservation.db");
    private static final String URL = "jdbc:sqlite:" + DB_FILE + "?foreign_keys=on";
    private static final String SCHEMA_RESOURCE = "/schema.sql";

    private DatabaseManager() {
    }

    /**
     * Opens a new connection to the SQLite database (the file is created if missing).
     * The caller must close it.
     *
     * @return an open connection with foreign keys enforced
     * @throws SQLException if the driver is missing or the database cannot be opened
     */
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("The SQLite JDBC driver was not found on the classpath.", e);
        }
        return DriverManager.getConnection(URL);
    }

    /**
     * Creates all tables if they do not exist yet.
     *
     * @throws SQLException if the schema cannot be applied
     */
    public static void initialize() throws SQLException {
        List<String> statements = loadSchemaStatements();
        try (Connection conn = getConnection(); Statement st = conn.createStatement()) {
            for (String sql : statements) {
                st.execute(sql);
            }
        }
    }

    /**
     * Reads {@code schema.sql} from the classpath and splits it into single statements.
     * Comment lines starting with {@code --} are ignored.
     *
     * @return the list of SQL statements, in file order
     * @throws SQLException if the schema resource cannot be read
     */
    public static List<String> loadSchemaStatements() throws SQLException {
        try (InputStream in = DatabaseManager.class.getResourceAsStream(SCHEMA_RESOURCE)) {
            if (in == null) {
                throw new SQLException("Schema resource " + SCHEMA_RESOURCE + " is missing from the application.");
            }
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().startsWith("--")) {
                        sb.append(line).append('\n');
                    }
                }
            }
            List<String> result = new ArrayList<>();
            for (String part : sb.toString().split(";")) {
                String sql = part.trim();
                if (!sql.isEmpty()) {
                    result.add(sql);
                }
            }
            return result;
        } catch (IOException e) {
            throw new SQLException("Could not read the database schema.", e);
        }
    }
}
