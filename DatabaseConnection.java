
package org.rpms;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // Update these with your actual database details
    private static final String URL = "jdbc:mysql://localhost:3306/rpms";
    private static final String USER = "USER NAME";
    private static final String PASSWORD = "PASSWORD HERE"; // Replace with your actual MySQL password

    public static Connection getConnection() throws SQLException {
        try {
            // Explicitly load the MySQL driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Create and return connection
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Database connected successfully!");
            return conn;

        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL Driver not found", e);
        }
    }
}