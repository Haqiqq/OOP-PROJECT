package org.rpms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class LoginLogger {

    private static final String INSERT_LOGIN_SQL =
            "INSERT INTO login_history (username, role) VALUES (?, ?)";

    public static void log(String email, String role) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_LOGIN_SQL)) {

            stmt.setString(1, email);
            stmt.setString(2, role);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error logging login attempt: " + e.getMessage());
        }
    }
}
